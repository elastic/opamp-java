package co.elastic.opamp.client.internal.request.handlers.sleeper;

public interface SleeperHandler {
  void awakeOrIgnoreNextSleep();

  void sleep() throws InterruptedException;
}
