package co.elastic.opamp.client.internal.request.tools;

public interface ThreadSleeper {
  void sleep() throws InterruptedException;
}
