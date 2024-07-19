package co.elastic.opamp.client.internal.request.tools;

import java.time.Duration;

public class FixedSleepSchedule implements SleepSchedule {
  private final long intervalMillis;
  private final Object sleepLock;
  private boolean isSleeping = false;
  private boolean ignoreNextSleep = false;

  public static FixedSleepSchedule of(Duration interval) {
    return new FixedSleepSchedule(interval.toMillis(), new Object());
  }

  private FixedSleepSchedule(long intervalMillis, Object sleepLock) {
    this.intervalMillis = intervalMillis;
    this.sleepLock = sleepLock;
  }

  @Override
  public synchronized void awakeOrIgnoreNextSleep() {
    if (isSleeping) {
      sleepLock.notify();
    } else {
      ignoreNextSleep = true;
    }
  }

  @Override
  public synchronized void sleep() throws InterruptedException {
    if (!ignoreNextSleep) {
      isSleeping = true;
      sleepLock.wait(intervalMillis);
      isSleeping = false;
    } else {
      ignoreNextSleep = false;
    }
  }
}
