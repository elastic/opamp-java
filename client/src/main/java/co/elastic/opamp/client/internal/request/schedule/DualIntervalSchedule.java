package co.elastic.opamp.client.internal.request.schedule;

import co.elastic.opamp.client.request.schedule.IntervalSchedule;

public final class DualIntervalSchedule implements IntervalSchedule {
  private final IntervalSchedule main;
  private final IntervalSchedule secondary;
  private IntervalSchedule current;

  public static DualIntervalSchedule of(IntervalSchedule main, IntervalSchedule secondary) {
    DualIntervalSchedule dualSchedule = new DualIntervalSchedule(main, secondary);
    dualSchedule.switchToMain();
    return dualSchedule;
  }

  private DualIntervalSchedule(IntervalSchedule main, IntervalSchedule secondary) {
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
