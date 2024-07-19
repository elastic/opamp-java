package co.elastic.opamp.client.internal.request.tools;

/** Wrapper for {@link Object#wait()} and {@link Object#notify()} to make them testable. */
public interface Sleeper {
  static Sleeper create() {
    return new SleeperImpl();
  }

  void sleep(long millis) throws InterruptedException;

  void awake();
}
