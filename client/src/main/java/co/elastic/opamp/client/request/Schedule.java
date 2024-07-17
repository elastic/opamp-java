package co.elastic.opamp.client.request;

import co.elastic.opamp.client.request.impl.FixedSchedule;
import java.time.Duration;

public interface Schedule {
  static Schedule fixed(Duration interval) {
    return FixedSchedule.of(interval);
  }

  boolean isDue();

  boolean fastForward();

  void start();
}
