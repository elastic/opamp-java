package co.elastic.opamp.client.internal.request;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestScheduler {
  private final ScheduledExecutorService executor;
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

  public synchronized void dispatchAfter(long delay, TimeUnit unit) {
    clearSchedule();
    currentSchedule = executor.schedule(requestRunner, delay, unit);
  }

  public synchronized void dispatchNow() {
    clearSchedule();
    currentSchedule = executor.submit(requestRunner);
  }

  public void setRequestRunner(Runnable requestRunner) {
    this.requestRunner = requestRunner;
  }

  private synchronized void clearSchedule() {
    if (currentSchedule != null && !currentSchedule.isDone() && !currentSchedule.isCancelled()) {
      currentSchedule.cancel(false);
    }
    currentSchedule = null;
  }
}
