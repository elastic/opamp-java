package co.elastic.opamp.client.state;

import co.elastic.opamp.client.internal.state.StateHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import opamp.proto.Anyvalue;
import opamp.proto.Opamp;

public class AgentDescriptionState extends StateHolder<Opamp.AgentDescription> {

  public static AgentDescriptionState create(Map<String, String> identifyingValues) {
    List<Anyvalue.KeyValue> identifyingAttributes = new ArrayList<>();
    identifyingValues.forEach(
        (key, value) -> {
          identifyingAttributes.add(createKeyValue(key, value));
        });
    return new AgentDescriptionState(
        Opamp.AgentDescription.newBuilder()
            .addAllIdentifyingAttributes(identifyingAttributes)
            .build());
  }

  private static Anyvalue.KeyValue createKeyValue(String key, String value) {
    return Anyvalue.KeyValue.newBuilder()
        .setKey(key)
        .setValue(Anyvalue.AnyValue.newBuilder().setStringValue(value).build())
        .build();
  }

  private AgentDescriptionState(Opamp.AgentDescription initialState) {
    super(initialState);
  }
}
