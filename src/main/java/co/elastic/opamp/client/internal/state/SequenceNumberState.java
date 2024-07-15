package co.elastic.opamp.client.internal.state;

public class SequenceNumberState extends StateHolder<Integer> {

  public static SequenceNumberState create(int initialValue) {
    return new SequenceNumberState(initialValue);
  }

  private SequenceNumberState(Integer initialState) {
    super(initialState);
  }
}
