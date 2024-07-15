package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.scheduler.MessageScheduler;
import co.elastic.opamp.client.internal.state.SequenceNumberState;
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
import co.elastic.opamp.client.state.AgentDescriptionState;
import co.elastic.opamp.client.state.EffectiveConfigState;
import co.elastic.opamp.client.state.RemoteConfigStatusState;
import java.util.Collections;
import opamp.proto.Opamp;

public final class OpampClientBuilder {
  private Service service = Service.create("http://localhost:4320");
  private AgentDescriptionState agentDescriptionState =
      AgentDescriptionState.create(Collections.emptyMap());
  private EffectiveConfigState effectiveConfigState =
      EffectiveConfigState.create(Opamp.EffectiveConfig.getDefaultInstance());
  private RemoteConfigStatusState remoteConfigStatusState = RemoteConfigStatusState.create();

  public OpampClientBuilder setHttpService(Service service) {
    this.service = service;
    return this;
  }

  public OpampClientBuilder setAgentDescriptionState(AgentDescriptionState agentDescriptionState) {
    this.agentDescriptionState = agentDescriptionState;
    return this;
  }

  public OpampClientBuilder setEffectiveConfigState(EffectiveConfigState effectiveConfigState) {
    this.effectiveConfigState = effectiveConfigState;
    return this;
  }

  public OpampClientBuilder setRemoteConfigStatusState(
      RemoteConfigStatusState remoteConfigStatusState) {
    this.remoteConfigStatusState = remoteConfigStatusState;
    return this;
  }

  public OpampClient build(OpampClient.Callback callback) {
    SequenceNumberState sequenceNumberState = SequenceNumberState.create(1);
    OpampClientVisitors visitors =
        new OpampClientVisitors(
            AgentDescriptionVisitor.create(agentDescriptionState),
            EffectiveConfigVisitor.create(effectiveConfigState),
            RemoteConfigStatusVisitor.create(remoteConfigStatusState),
            SequenceNumberVisitor.create(sequenceNumberState),
            new CapabilitiesVisitor(),
            new FlagsVisitor(),
            new InstanceUidVisitor(),
            new AgentDisconnectVisitor());
    MessageScheduler messageScheduler = MessageScheduler.create(this.service);
    return OpampClientImpl.create(
        messageScheduler, RequestContext.newBuilder(), visitors, callback);
  }
}
