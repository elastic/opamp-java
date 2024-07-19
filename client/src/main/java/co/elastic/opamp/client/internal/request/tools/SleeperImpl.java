package co.elastic.opamp.client.internal.request.tools;

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
