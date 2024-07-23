package co.elastic.opamp.client.internal.state;

public final class SequenceNumberState extends StateHolder<Integer> {

  static SequenceNumberState create() {
    return new SequenceNumberState(1);
  }

  private SequenceNumberState(Integer initialState) {
    super(initialState);
  }

  public void increment() {
    set(get() + 1);
  }
}
