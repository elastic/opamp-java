package co.elastic.opamp.client;

import co.elastic.opamp.client.handlers.InstanceUidHandler;
import co.elastic.opamp.client.internal.OpampClientImpl;
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
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.request.Schedule;
import co.elastic.opamp.client.request.impl.FixedSchedule;
import co.elastic.opamp.client.request.impl.OkHttpRequestSender;
import java.time.Duration;
import opamp.proto.Anyvalue;

public final class OpampClientBuilder {
  private RequestSender sender = OkHttpRequestSender.create("http://localhost:4320");
  private InstanceUidHandler instanceUidHandler = InstanceUidHandler.getDefault();
  private Schedule pollingSchedule = FixedSchedule.create(Duration.ofSeconds(30));
  private Schedule retrySchedule;
  private final OpampClientState state = OpampClientState.create();

  public OpampClientBuilder setMessageSender(RequestSender sender) {
    this.sender = sender;
    return this;
  }

  public OpampClientBuilder setInstanceUidHandler(InstanceUidHandler instanceUidHandler) {
    this.instanceUidHandler = instanceUidHandler;
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
            InstanceUidVisitor.create(instanceUidHandler),
            new AgentDisconnectVisitor());
    return OpampClientImpl.create(sender, visitors, state, pollingSchedule, retrySchedule, callback);
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
