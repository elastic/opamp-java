package co.elastic.opamp.client.internal.request.tools;

public interface SleepSchedule {
  void awakeOrIgnoreNextSleep();

  void sleep() throws InterruptedException;
}
