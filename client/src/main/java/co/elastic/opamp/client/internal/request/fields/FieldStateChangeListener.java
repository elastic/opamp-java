package co.elastic.opamp.client.internal.request.fields;

public interface FieldStateChangeListener {
  void onStateForFieldChanged(FieldType fieldType);
}
