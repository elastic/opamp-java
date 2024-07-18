package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.request.schedule.Schedule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RequestDispatcher implements Runnable {
  private final ExecutorService executor;
  private final Schedule regularSchedule;
  private final Schedule retrySchedule;
  private Schedule currentSchedule;
  private final Object scheduleLock = new Object();
  private final Object runningLock = new Object();
  private boolean isRunning = false;
  private Runnable requestRunner;

  RequestDispatcher(ExecutorService executor, Schedule regularSchedule, Schedule retrySchedule) {
    this.executor = executor;
    this.regularSchedule = regularSchedule;
    this.retrySchedule = retrySchedule;
  }

  public static RequestDispatcher create(Schedule regularSchedule, Schedule retrySchedule) {
    return new RequestDispatcher(
        Executors.newSingleThreadExecutor(), regularSchedule, retrySchedule);
  }

  public synchronized void start(Runnable requestRunner) {
    this.requestRunner = requestRunner;
    isRunning = true;
    useRegularSchedule();
    executor.execute(this);
  }

  public synchronized void stop() {
    executor.shutdown();
    isRunning = false;
  }

  public void enableRetryMode() {
    useRetrySchedule();
  }

  public void disableRetryMode() {
    useRegularSchedule();
  }

  @Override
  public void run() {
    while (true) {
      try {
        boolean due;
        synchronized (scheduleLock) {
          due = currentSchedule.isDue();
        }
        if (due) requestRunner.run();
        synchronized (runningLock) {
          if (!isRunning) {
            break;
          }
        }
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private void useRegularSchedule() {
    setCurrentSchedule(regularSchedule);
  }

  private void useRetrySchedule() {
    setCurrentSchedule(retrySchedule);
  }

  private void setCurrentSchedule(Schedule schedule) {
    synchronized (scheduleLock) {
      currentSchedule = schedule;
    }
  }
}
