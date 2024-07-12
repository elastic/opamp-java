package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.RequestContext;
import java.util.ArrayList;
import java.util.List;
import opamp.proto.Anyvalue;
import opamp.proto.Opamp;

public class AgentDescriptionVisitor implements AgentToServerVisitor {
  private final List<Anyvalue.KeyValue> identifyingAttributes = new ArrayList<>();

  public AgentDescriptionVisitor(String serviceName, String serviceVersion) {
    identifyingAttributes.add(createKeyValue("service.name", serviceName));
    identifyingAttributes.add(createKeyValue("service.version", serviceVersion));
  }

  private Anyvalue.KeyValue createKeyValue(String key, String value) {
    return Anyvalue.KeyValue.newBuilder()
        .setKey(key)
        .setValue(Anyvalue.AnyValue.newBuilder().setStringValue(value).build())
        .build();
  }

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    Opamp.AgentDescription agentDescription =
        Opamp.AgentDescription.newBuilder()
            .addAllIdentifyingAttributes(identifyingAttributes)
            .build();
    builder.setAgentDescription(agentDescription);
  }
}
