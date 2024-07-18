package co.elastic.opamp.client.request.handlers;

import co.elastic.opamp.client.internal.request.handlers.FixedIntervalHandler;
import java.time.Duration;

public interface IntervalHandler {
  static IntervalHandler fixed(Duration interval) {
    return FixedIntervalHandler.of(interval);
  }

  boolean isDue();

  boolean fastForward();

  void startNext();

  void suggestNextInterval(Duration interval);

  void reset();
}
