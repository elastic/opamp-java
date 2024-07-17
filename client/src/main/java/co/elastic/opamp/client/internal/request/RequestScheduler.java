package co.elastic.opamp.client.internal.request;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestScheduler {
  private final ScheduledExecutorService executor;
  private boolean isRunning = false;
  private Runnable requestRunner;
  private Future<?> currentSchedule;

  RequestScheduler(ScheduledExecutorService executor) {
    this.executor = executor;
  }

  public static RequestScheduler create() {
    ScheduledThreadPoolExecutor executor =
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    executor.setRemoveOnCancelPolicy(true);
    return new RequestScheduler(executor);
  }

  public synchronized void start(Runnable requestRunner) {
    this.requestRunner = requestRunner;
    isRunning = true;
    dispatchNow();
  }

  public synchronized void scheduleImmediatelyAndStop() {
    dispatchNow(); // todo stop
    isRunning = false;
  }

  public void scheduleImmediately() {
    dispatchNow();
  }

  public void scheduleNext() {
    dispatchAfter(30, TimeUnit.SECONDS);
  }

  public void retry() {
    // todo
  }

  private synchronized void dispatchNow() {
    dispatchAfter(0, TimeUnit.MILLISECONDS);
  }

  private synchronized void dispatchAfter(long delay, TimeUnit unit) {
    if (!isRunning) {
      return;
    }
    clearSchedule();
    currentSchedule = executor.schedule(requestRunner, delay, unit);
  }

  private synchronized void clearSchedule() {
    if (currentSchedule != null && !currentSchedule.isDone() && !currentSchedule.isCancelled()) {
      currentSchedule.cancel(false);
    }
    currentSchedule = null;
  }
}
