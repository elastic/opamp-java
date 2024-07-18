package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.internal.request.schedule.DualIntervalSchedule;
import co.elastic.opamp.client.request.schedule.IntervalSchedule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RequestDispatcher implements Runnable {
  private final ExecutorService executor;
  private final DualIntervalSchedule schedule;
  private final Object runningLock = new Object();
  private boolean isRunning = false;
  private Runnable requestRunner;

  RequestDispatcher(ExecutorService executor, DualIntervalSchedule schedule) {
    this.executor = executor;
    this.schedule = schedule;
  }

  public static RequestDispatcher create(IntervalSchedule pollingSchedule, IntervalSchedule retrySchedule) {
    return new RequestDispatcher(
        Executors.newSingleThreadExecutor(), DualIntervalSchedule.of(pollingSchedule, retrySchedule));
  }

  public void start(Runnable requestRunner) {
    synchronized (runningLock) {
      this.requestRunner = requestRunner;
      schedule.startNext();
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
          schedule.startNext();
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
