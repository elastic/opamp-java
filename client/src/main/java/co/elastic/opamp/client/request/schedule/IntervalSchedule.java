package co.elastic.opamp.client.request.schedule;

import java.time.Duration;

public interface IntervalSchedule {
  static IntervalSchedule fixed(Duration interval) {
    return FixedIntervalSchedule.of(interval);
  }

  boolean isDue();

  boolean fastForward();

  void startNext();

  void reset();
}
