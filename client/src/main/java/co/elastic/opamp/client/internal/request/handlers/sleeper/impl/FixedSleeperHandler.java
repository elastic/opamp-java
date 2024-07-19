package co.elastic.opamp.client.internal.request.handlers.sleeper.impl;

import co.elastic.opamp.client.internal.request.handlers.sleeper.Sleeper;
import co.elastic.opamp.client.internal.request.handlers.sleeper.SleeperHandler;
import java.time.Duration;

public final class FixedSleeperHandler implements SleeperHandler {
  private final long intervalMillis;
  private final Sleeper sleeper;
  private boolean isSleeping = false;
  private boolean ignoreNextSleep = false;

  public static FixedSleeperHandler of(Duration interval) {
    return new FixedSleeperHandler(interval.toMillis(), Sleeper.create());
  }

  FixedSleeperHandler(long intervalMillis, Sleeper sleeper) {
    this.intervalMillis = intervalMillis;
    this.sleeper = sleeper;
  }

  @Override
  public synchronized void awakeOrIgnoreNextSleep() {
    if (isSleeping) {
      sleeper.awake();
    } else {
      ignoreNextSleep = true;
    }
  }

  @Override
  public void sleep() throws InterruptedException {
    synchronized (this) {
      if (ignoreNextSleep) {
        ignoreNextSleep = false;
        return;
      }
      isSleeping = true;
    }
    sleeper.sleep(intervalMillis);
    synchronized (this) {
      isSleeping = false;
    }
  }
}
