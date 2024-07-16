package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.dispatcher.MessageDispatcher;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.request.MessageSender;
import opamp.proto.Anyvalue;

public final class OpampClientBuilder {
  private MessageSender sender = MessageSender.create("http://localhost:4320");
  private final OpampClientState state = OpampClientState.create();

  public OpampClientBuilder setMessageSender(MessageSender sender) {
    this.sender = sender;
    return this;
  }

  public OpampClientBuilder setServiceName(String serviceName) {
    addIdentifyingAttribute("service.name", serviceName);
    return this;
  }

  public OpampClientBuilder setServiceVersion(String serviceVersion) {
    addIdentifyingAttribute("service.version", serviceVersion);
    return this;
  }

  public OpampClient build(OpampClient.Callback callback) {
    OpampClientVisitors visitors =
        new OpampClientVisitors(
            AgentDescriptionVisitor.create(state.agentDescriptionState),
            EffectiveConfigVisitor.create(state.effectiveConfigState),
            RemoteConfigStatusVisitor.create(state.remoteConfigStatusState),
            SequenceNumberVisitor.create(state.sequenceNumberState),
            new CapabilitiesVisitor(),
            new FlagsVisitor(),
            new InstanceUidVisitor(),
            new AgentDisconnectVisitor());
    MessageDispatcher dispatcher = MessageDispatcher.create(sender);
    return OpampClientImpl.create(
        dispatcher, RequestContext.newBuilder(), visitors, state, callback);
  }

  private void addIdentifyingAttribute(String key, String value) {
    state
        .agentDescriptionState
        .get()
        .getIdentifyingAttributesList()
        .add(createKeyValue(key, value));
  }

  private Anyvalue.KeyValue createKeyValue(String key, String value) {
    return Anyvalue.KeyValue.newBuilder()
        .setKey(key)
        .setValue(Anyvalue.AnyValue.newBuilder().setStringValue(value).build())
        .build();
  }
}
