package co.elastic.opamp.client.internal.request.fields;

import co.elastic.opamp.client.state.observer.Observable;
import co.elastic.opamp.client.state.observer.Observer;

public final class FieldStateObserver implements Observer {
  private final FieldStateChangeListener listener;
  private final FieldType fieldType;

  public FieldStateObserver(FieldStateChangeListener listener, FieldType fieldType) {
    this.listener = listener;
    this.fieldType = fieldType;
  }

  @Override
  public void update(Observable observable) {
    listener.onStateForFieldChanged(fieldType);
  }
}
