package co.elastic.opamp.client.request.impl;

import co.elastic.opamp.client.request.Schedule;
import java.time.Duration;
import java.util.function.Supplier;

public final class FixedSchedule implements Schedule {
  private final long intervalMillis;
  private final Supplier<Long> currentTimeMillisSupplier;
  private long startTimeMillis;
  private boolean forceDue = false;

  public static FixedSchedule create(Duration interval) {
    return new FixedSchedule(interval.toMillis(), System::currentTimeMillis);
  }

  FixedSchedule(long intervalMillis, Supplier<Long> currentTimeMillisSupplier) {
    this.intervalMillis = intervalMillis;
    this.currentTimeMillisSupplier = currentTimeMillisSupplier;
    startTimeMillis = currentTimeMillisSupplier.get();
  }

  @Override
  public boolean isDue() {
    if (forceDue) {
      return true;
    }
    return intervalMillis >= currentTimeMillisSupplier.get() - startTimeMillis;
  }

  @Override
  public boolean fastForward() {
    forceDue = true;
    return true;
  }

  @Override
  public void reset() {
    forceDue = false;
    startTimeMillis = currentTimeMillisSupplier.get();
  }
}
