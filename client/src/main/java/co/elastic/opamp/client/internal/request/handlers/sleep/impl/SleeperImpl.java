package co.elastic.opamp.client.internal.request.handlers.sleep.impl;

import co.elastic.opamp.client.internal.request.handlers.sleep.Sleeper;

/** Wrapper for {@link Object#wait()} and {@link Object#notify()} to make them testable. */
public final class SleeperImpl implements Sleeper {
  @Override
  public synchronized void sleep(long millis) throws InterruptedException {
    wait(millis);
  }

  @Override
  public synchronized void awake() {
    notify();
  }
}
