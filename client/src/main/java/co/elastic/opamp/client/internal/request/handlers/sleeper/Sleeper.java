package co.elastic.opamp.client.internal.request.handlers.sleeper;

import co.elastic.opamp.client.internal.request.handlers.sleeper.impl.SleeperImpl;

public interface Sleeper {
  static Sleeper create() {
    return new SleeperImpl();
  }

  void sleep(long millis) throws InterruptedException;

  void awake();
}
