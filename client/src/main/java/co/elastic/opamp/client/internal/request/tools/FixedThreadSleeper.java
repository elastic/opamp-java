package co.elastic.opamp.client.internal.request.tools;

import java.time.Duration;

public class FixedThreadSleeper implements ThreadSleeper {
  private final long intervalMillis;

  public static FixedThreadSleeper of(Duration interval) {
    return new FixedThreadSleeper(interval.toMillis());
  }

  private FixedThreadSleeper(long intervalMillis) {
    this.intervalMillis = intervalMillis;
  }

  @Override
  public void sleep() throws InterruptedException {
    Thread.sleep(intervalMillis);
  }
}
