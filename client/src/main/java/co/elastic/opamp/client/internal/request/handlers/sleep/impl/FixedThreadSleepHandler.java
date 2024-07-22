package co.elastic.opamp.client.internal.request.handlers.sleep.impl;

import co.elastic.opamp.client.internal.request.handlers.sleep.Sleeper;
import co.elastic.opamp.client.internal.request.handlers.sleep.ThreadSleepHandler;
import java.time.Duration;

public final class FixedThreadSleepHandler implements ThreadSleepHandler {
  private final long intervalMillis;
  private final Sleeper sleeper;
  private final Object sleepingLock = new Object();
  private boolean isSleeping = false;
  private boolean ignoreNextSleep = false;

  public static FixedThreadSleepHandler of(Duration interval) {
    return new FixedThreadSleepHandler(interval.toMillis(), Sleeper.create());
  }

  FixedThreadSleepHandler(long intervalMillis, Sleeper sleeper) {
    this.intervalMillis = intervalMillis;
    this.sleeper = sleeper;
  }

  @Override
  public void awakeOrIgnoreNextSleep() {
    synchronized (sleepingLock) {
      if (isSleeping) {
        sleeper.awake();
      } else {
        ignoreNextSleep = true;
      }
    }
  }

  @Override
  public void sleep() throws InterruptedException {
    synchronized (sleepingLock) {
      if (ignoreNextSleep) {
        ignoreNextSleep = false;
        return;
      }
      isSleeping = true;
    }
    sleeper.sleep(intervalMillis);
    synchronized (sleepingLock) {
      isSleeping = false;
    }
  }
}
