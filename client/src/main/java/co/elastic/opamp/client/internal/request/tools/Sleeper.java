package co.elastic.opamp.client.internal.request.tools;

public interface Sleeper {
  static Sleeper create() {
    return new SleeperImpl();
  }

  void sleep(long millis) throws InterruptedException;

  void awake();
}
