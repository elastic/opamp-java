package co.elastic.opamp.client.internal.request.schedule;

import co.elastic.opamp.client.request.schedule.Schedule;

public final class DualSchedule implements Schedule {
  private final Schedule main;
  private final Schedule secondary;
  private Schedule current;

  public static DualSchedule of(Schedule main, Schedule secondary) {
    DualSchedule dualSchedule = new DualSchedule(main, secondary);
    dualSchedule.switchToMain();
    return dualSchedule;
  }

  private DualSchedule(Schedule main, Schedule secondary) {
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
