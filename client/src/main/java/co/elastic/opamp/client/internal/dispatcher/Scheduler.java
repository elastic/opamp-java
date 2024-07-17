package co.elastic.opamp.client.internal.dispatcher;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Scheduler {
  private final ScheduledExecutorService executor;
  private final RequestDispatcher dispatcher;
  private Future<?> currentSchedule;

  public Scheduler(ScheduledExecutorService executor, RequestDispatcher dispatcher) {
    this.executor = executor;
    this.dispatcher = dispatcher;
  }

  public static Scheduler create(RequestDispatcher dispatcher) {
    ScheduledThreadPoolExecutor executor =
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    executor.setRemoveOnCancelPolicy(true);
    return new Scheduler(executor, dispatcher);
  }

  public synchronized void dispatchAfter(long delay, TimeUnit unit) {
    clearSchedule();
    currentSchedule = executor.schedule(dispatcher, delay, unit);
  }

  public synchronized void dispatchNow() {
    clearSchedule();
    currentSchedule = executor.submit(dispatcher);
  }

  private synchronized void clearSchedule() {
    if (currentSchedule != null && !currentSchedule.isDone() && !currentSchedule.isCancelled()) {
      currentSchedule.cancel(false);
    }
    currentSchedule = null;
  }
}
