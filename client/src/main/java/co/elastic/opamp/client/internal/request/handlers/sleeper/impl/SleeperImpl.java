package co.elastic.opamp.client.internal.request.handlers.sleeper.impl;

import co.elastic.opamp.client.internal.request.handlers.sleeper.Sleeper;

/** Wrapper for {@link Object#wait()} and {@link Object#notify()} to make them testable. */
public final class SleeperImpl implements Sleeper {
  @Override
  public void sleep(long millis) throws InterruptedException {
    wait(millis);
  }

  @Override
  public void awake() {
    notify();
  }
}
