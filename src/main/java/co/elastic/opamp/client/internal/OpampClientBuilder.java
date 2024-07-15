package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.dispatcher.MessageScheduler;
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
import co.elastic.opamp.client.request.Service;
import opamp.proto.Opamp;

public final class OpampClientBuilder {
  private Service service = Service.create("http://localhost:4320");
  private final OpampClientState state = OpampClientState.create();

  public OpampClientBuilder setHttpService(Service service) {
    this.service = service;
    return this;
  }

  public OpampClientBuilder setAgentDescription(Opamp.AgentDescription agentDescription) {
    state.agentDescriptionState.set(agentDescription);
    return this;
  }

  public OpampClientBuilder setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig) {
    state.effectiveConfigState.set(effectiveConfig);
    return this;
  }

  public OpampClientBuilder setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus) {
    state.remoteConfigStatusState.set(remoteConfigStatus);
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
    MessageScheduler messageScheduler = MessageScheduler.create(service);
    return OpampClientImpl.create(
        messageScheduler, RequestContext.newBuilder(), visitors, state, callback);
  }
}
