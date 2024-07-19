package co.elastic.opamp.client.internal.request.handlers.sleeper;

public interface ThreadSleepHandler {
  void awakeOrIgnoreNextSleep();

  void sleep() throws InterruptedException;
}
