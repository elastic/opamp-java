package co.elastic.opamp.client.internal.request.tools;

/** Wrapper for {@link Object#wait()} and {@link Object#notify()} to make them testable. */
final class SleeperImpl implements Sleeper {
  @Override
  public void sleep(long millis) throws InterruptedException {
    wait(millis);
  }

  @Override
  public void awake() {
    notify();
  }
}
