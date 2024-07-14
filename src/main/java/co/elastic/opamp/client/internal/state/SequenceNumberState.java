package co.elastic.opamp.client.internal.state;

public class SequenceNumberState extends StateHolder<Integer> {

  public static SequenceNumberState create() {
    return new SequenceNumberState(1);
  }

  private SequenceNumberState(Integer initialState) {
    super(initialState);
  }
}
