package co.elastic.opamp.client.request;

public interface Schedule {
  boolean isDue();

  boolean fastForward();

  void reset();
}
