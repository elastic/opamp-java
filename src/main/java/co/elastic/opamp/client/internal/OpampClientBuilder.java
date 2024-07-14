package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.state.SequenceNumberState;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.request.HttpService;
import co.elastic.opamp.client.state.AgentDescriptionState;
import co.elastic.opamp.client.state.EffectiveConfigState;
import co.elastic.opamp.client.state.RemoteConfigStatusState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import opamp.proto.Opamp;

public final class OpampClientBuilder {
  private HttpService httpService = HttpService.create("http://localhost:4320");
  private AgentDescriptionState agentDescriptionState =
      AgentDescriptionState.create(Collections.emptyMap());
  private EffectiveConfigState effectiveConfigState =
      EffectiveConfigState.create(Opamp.EffectiveConfig.getDefaultInstance());
  private RemoteConfigStatusState remoteConfigStatusState = RemoteConfigStatusState.create();

  public OpampClientBuilder setHttpService(HttpService httpService) {
    this.httpService = httpService;
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
    SequenceNumberState sequenceNumberState = SequenceNumberState.create();
    List<AgentToServerVisitor> visitors = new ArrayList<>();
    visitors.add(AgentDescriptionVisitor.create(agentDescriptionState));
    visitors.add(EffectiveConfigVisitor.create(effectiveConfigState));
    visitors.add(RemoteConfigStatusVisitor.create(remoteConfigStatusState));
    visitors.add(SequenceNumberVisitor.create(sequenceNumberState));
    visitors.add(new CapabilitiesVisitor());
    visitors.add(new FlagsVisitor());
    visitors.add(new InstanceUidVisitor());
    visitors.add(new AgentDisconnectVisitor());
    return new OpampClientImpl(httpService, RequestContext.newBuilder(), callback, visitors);
  }
}
