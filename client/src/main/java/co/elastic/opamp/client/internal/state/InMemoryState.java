package co.elastic.opamp.client.internal.state;

public abstract class InMemoryState<T> extends State<T> {
  private T state;

  public InMemoryState(T initialState) {
    this.state = initialState;
  }

  public synchronized void set(T value) {
    if (!areEqual(state, value)) {
      state = value;
      notifyObservers();
    }
  }

  @Override
  public synchronized T get() {
    return state;
  }

  protected boolean areEqual(T first, T second) {
    return first.equals(second);
  }
}
