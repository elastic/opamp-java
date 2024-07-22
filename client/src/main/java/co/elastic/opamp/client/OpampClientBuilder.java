package co.elastic.opamp.client;

import co.elastic.opamp.client.internal.OpampClientImpl;
import co.elastic.opamp.client.internal.request.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.request.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.request.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.request.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.request.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.request.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.request.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.request.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.request.handlers.IntervalHandler;
import co.elastic.opamp.client.request.impl.OkHttpRequestSender;
import java.time.Duration;
import opamp.proto.Anyvalue;
import opamp.proto.Opamp;

public final class OpampClientBuilder {
  private RequestSender sender = OkHttpRequestSender.create("http://localhost:4320");
  private IntervalHandler pollingIntervalHandler = IntervalHandler.fixed(Duration.ofSeconds(30));
  private IntervalHandler retryIntervalHandler = IntervalHandler.fixed(Duration.ofSeconds(30));
  private final OpampClientState state = OpampClientState.create();

  public OpampClientBuilder setRequestSender(RequestSender sender) {
    this.sender = sender;
    return this;
  }

  public OpampClientBuilder setInstanceUid(byte[] instanceUid) {
    state.instanceUidState.set(instanceUid);
    return this;
  }

  public OpampClientBuilder setServiceName(String serviceName) {
    addIdentifyingAttribute("service.name", serviceName);
    return this;
  }

  public OpampClientBuilder setServiceNamespace(String serviceNamespace) {
    addIdentifyingAttribute("service.namespace", serviceNamespace);
    return this;
  }

  public OpampClientBuilder setServiceVersion(String serviceVersion) {
    addIdentifyingAttribute("service.version", serviceVersion);
    return this;
  }

  public OpampClientBuilder setPollingIntervalHandler(IntervalHandler pollingIntervalHandler) {
    this.pollingIntervalHandler = pollingIntervalHandler;
    return this;
  }

  public OpampClientBuilder setRetryIntervalHandler(IntervalHandler retryIntervalHandler) {
    this.retryIntervalHandler = retryIntervalHandler;
    return this;
  }

  public OpampClientBuilder enableRemoteConfig() {
    state.capabilitiesState.add(
        Opamp.AgentCapabilities.AgentCapabilities_AcceptsRemoteConfig_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_ReportsRemoteConfig_VALUE);
    return this;
  }

  public OpampClientBuilder enableEffectiveConfigReporting() {
    state.capabilitiesState.add(
        Opamp.AgentCapabilities.AgentCapabilities_ReportsEffectiveConfig_VALUE);
    return this;
  }

  public OpampClient build(OpampClient.Callback callback) {
    OpampClientVisitors visitors =
        new OpampClientVisitors(
            AgentDescriptionVisitor.create(state.agentDescriptionState),
            EffectiveConfigVisitor.create(state.effectiveConfigState),
            RemoteConfigStatusVisitor.create(state.remoteConfigStatusState),
            SequenceNumberVisitor.create(state.sequenceNumberState),
            CapabilitiesVisitor.create(state.capabilitiesState),
            InstanceUidVisitor.create(state.instanceUidState),
            FlagsVisitor.create(),
            AgentDisconnectVisitor.create());
    return OpampClientImpl.create(
        sender, visitors, state, pollingIntervalHandler, retryIntervalHandler, callback);
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
