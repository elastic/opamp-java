package co.elastic.opamp.client.internal.periodicwork;

import java.time.Duration;

public interface PeriodicDelay {
  Duration getNextDelay();

  void reset();

  default long getNextDelayInNanos() {
    return getNextDelay().toNanos();
  }
}
