package co.elastic.opamp.client.internal.request.tools;

import java.time.Duration;

public final class FixedSleepSchedule implements SleepSchedule {
  private final long intervalMillis;
  private final Sleeper sleeper;
  private boolean isSleeping = false;
  private boolean ignoreNextSleep = false;

  public static FixedSleepSchedule of(Duration interval) {
    return new FixedSleepSchedule(interval.toMillis(), Sleeper.create());
  }

  FixedSleepSchedule(long intervalMillis, Sleeper sleeper) {
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
      if (isSleeping) {
        return;
      }
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
