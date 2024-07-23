package co.elastic.opamp.client.internal.request.handlers;

import co.elastic.opamp.client.request.handlers.IntervalHandler;
import java.time.Duration;
import java.util.function.Supplier;

public final class FixedIntervalHandler implements IntervalHandler {
  private final long intervalNanos;
  private final Supplier<Long> nanoTimeSupplier;
  private long startTimeNanos;
  private boolean firstCheck = true;
  private boolean forceDue = false;

  public static FixedIntervalHandler of(Duration interval) {
    return new FixedIntervalHandler(interval.toNanos(), System::nanoTime);
  }

  FixedIntervalHandler(long intervalNanos, Supplier<Long> nanoTimeSupplier) {
    this.intervalNanos = intervalNanos;
    this.nanoTimeSupplier = nanoTimeSupplier;
  }

  @Override
  public boolean isDue() {
    if (firstCheck) {
      firstCheck = false;
      return true;
    }
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
  public boolean suggestNextInterval(Duration interval) {
    // Ignored.
    return false;
  }

  @Override
  public void reset() {
    firstCheck = true;
    startNext();
  }
}
