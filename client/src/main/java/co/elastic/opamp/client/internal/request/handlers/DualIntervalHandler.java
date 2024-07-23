package co.elastic.opamp.client.internal.request.handlers;

import co.elastic.opamp.client.request.handlers.IntervalHandler;
import java.time.Duration;

public final class DualIntervalHandler implements IntervalHandler {
  private final IntervalHandler main;
  private final IntervalHandler secondary;
  private IntervalHandler current;

  public static DualIntervalHandler of(IntervalHandler main, IntervalHandler secondary) {
    DualIntervalHandler dualIntervalHandler = new DualIntervalHandler(main, secondary);
    dualIntervalHandler.switchToMain();
    return dualIntervalHandler;
  }

  private DualIntervalHandler(IntervalHandler main, IntervalHandler secondary) {
    this.main = main;
    this.secondary = secondary;
  }

  @Override
  public synchronized boolean isDue() {
    return current.isDue();
  }

  @Override
  public synchronized boolean fastForward() {
    return current.fastForward();
  }

  @Override
  public synchronized void startNext() {
    current.startNext();
  }

  @Override
  public synchronized boolean suggestNextInterval(Duration interval) {
    return current.suggestNextInterval(interval);
  }

  @Override
  public synchronized void reset() {
    current.reset();
  }

  public synchronized void switchToMain() {
    current = main;
  }

  public synchronized void switchToSecondary() {
    current = secondary;
  }
}
