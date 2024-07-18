package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.internal.request.schedule.DualSchedule;
import co.elastic.opamp.client.request.schedule.Schedule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RequestDispatcher implements Runnable {
  private final ExecutorService executor;
  private final DualSchedule schedule;
  private final Object runningLock = new Object();
  private boolean isRunning = false;
  private Runnable requestRunner;

  RequestDispatcher(ExecutorService executor, DualSchedule schedule) {
    this.executor = executor;
    this.schedule = schedule;
  }

  public static RequestDispatcher create(Schedule pollingSchedule, Schedule retrySchedule) {
    return new RequestDispatcher(
        Executors.newSingleThreadExecutor(), DualSchedule.of(pollingSchedule, retrySchedule));
  }

  public void start(Runnable requestRunner) {
    synchronized (runningLock) {
      this.requestRunner = requestRunner;
      schedule.start();
      executor.execute(this);
      isRunning = true;
    }
  }

  public void stop() {
    synchronized (runningLock) {
      executor.shutdown();
      isRunning = false;
    }
  }

  public void enableRetryMode() {
    schedule.switchToSecondary();
  }

  public void disableRetryMode() {
    schedule.switchToMain();
  }

  public void tryDispatchNow() {
    schedule.fastForward();
  }

  @Override
  public void run() {
    while (true) {
      try {
        if (schedule.isDue()) {
          requestRunner.run();
          schedule.start();
        }
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
}
