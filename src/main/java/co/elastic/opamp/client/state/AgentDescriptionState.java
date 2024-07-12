package co.elastic.opamp.client.state;

import co.elastic.opamp.client.internal.state.StateHolder;
import java.util.ArrayList;
import java.util.List;
import opamp.proto.Anyvalue;
import opamp.proto.Opamp;

public class AgentDescriptionState extends StateHolder<Opamp.AgentDescription> {

  public static AgentDescriptionState create() {
    List<Anyvalue.KeyValue> identifyingAttributes = new ArrayList<>();
    identifyingAttributes.add(createKeyValue("service.name", "undefined"));
    identifyingAttributes.add(createKeyValue("service.version", "undefined"));
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
