package co.elastic.opamp.client.request.schedule;

import java.time.Duration;
import java.util.function.Supplier;

public final class FixedSchedule implements Schedule {
  private final long intervalNanos;
  private final Supplier<Long> nanoTimeSupplier;
  private long startTimeNanos;
  private boolean forceDue = false;

  public static FixedSchedule of(Duration interval) {
    return new FixedSchedule(interval.toNanos(), System::nanoTime);
  }

  FixedSchedule(long intervalNanos, Supplier<Long> nanoTimeSupplier) {
    this.intervalNanos = intervalNanos;
    this.nanoTimeSupplier = nanoTimeSupplier;
  }

  @Override
  public boolean isDue() {
    if (forceDue) {
      return true;
    }
    return nanoTimeSupplier.get() - startTimeNanos >= intervalNanos;
  }

  @Override
  public boolean fastForward() {
    forceDue = true;
    return true;
  }

  @Override
  public void startNext() {
    forceDue = false;
    startTimeNanos = nanoTimeSupplier.get();
  }

  @Override
  public void reset() {
    startNext();
  }
}
