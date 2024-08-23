package co.elastic.opamp.client.internal.periodicwork;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class PeriodicTaskExecutor {
  private final ScheduledExecutorService executorService;
  private final Lock delaySetLock = new ReentrantLock();
  private PeriodicDelay periodicDelay;
  private ScheduledFuture<?> scheduledFuture;
  private Runnable periodicTask;

  public static PeriodicTaskExecutor create(PeriodicDelay initialPeriodicDelay) {
    return new PeriodicTaskExecutor(
        Executors.newSingleThreadScheduledExecutor(), initialPeriodicDelay);
  }

  PeriodicTaskExecutor(
      ScheduledExecutorService executorService, PeriodicDelay initialPeriodicDelay) {
    this.executorService = executorService;
    this.periodicDelay = initialPeriodicDelay;
  }

  public void start(Runnable periodicTask) {
    this.periodicTask = periodicTask;
    scheduleNext();
  }

  public void executeNow() {
    executorService.execute(periodicTask);
  }

  public void setPeriodicDelay(PeriodicDelay periodicDelay) {
    delaySetLock.lock();
    try {
      if (scheduledFuture != null) {
        scheduledFuture.cancel(false);
      }
      this.periodicDelay = periodicDelay;
      periodicDelay.reset();
      scheduleNext();
    } finally {
      delaySetLock.unlock();
    }
  }

  public void stop() {
    executorService.shutdown();
  }

  private void scheduleNext() {
    delaySetLock.lock();
    try {
      scheduledFuture =
          executorService.schedule(
              new PeriodicRunner(), periodicDelay.getNextDelayInNanos(), TimeUnit.NANOSECONDS);
    } finally {
      delaySetLock.unlock();
    }
  }

  private class PeriodicRunner implements Runnable {
    @Override
    public void run() {
      periodicTask.run();
      scheduleNext();
    }
  }
}
