package co.elastic.opamp.client.internal.request.handlers.sleep;

public interface ThreadSleepHandler {
  void awakeOrIgnoreNextSleep();

  void sleep() throws InterruptedException;
}
