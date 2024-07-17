package co.elastic.opamp.client.internal.state;

import co.elastic.opamp.client.internal.state.observer.Observable;
import java.util.function.Supplier;

public class StateHolder<T> extends Observable implements Supplier<T> {
  private T state;

  public StateHolder(T initialState) {
    this.state = initialState;
  }

  public synchronized void set(T value) {
    if (!state.equals(value)) {
      state = value;
      notifyObservers();
    }
  }

  @Override
  public synchronized T get() {
    return state;
  }
}