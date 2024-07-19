package co.elastic.opamp.client.internal.request.handlers.sleep;

import co.elastic.opamp.client.internal.request.handlers.sleep.impl.SleeperImpl;

/** Wrapper for {@link Object#wait()} and {@link Object#notify()} to make them testable. */
public interface Sleeper {
  static Sleeper create() {
    return new SleeperImpl();
  }

  void sleep(long millis) throws InterruptedException;

  void awake();
}
