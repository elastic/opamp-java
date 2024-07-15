package co.elastic.opamp.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.scheduler.Message;
import co.elastic.opamp.client.internal.scheduler.MessageScheduler;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.response.Response;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opamp.proto.Anyvalue;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OpampClientImplTest {
  private MessageScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = mock();
  }

  @Test
  void verifyStart() {
    buildClient(null).start();

    verify(scheduler).scheduleNow();
  }

  @Test
  void verifyMessageBuilding() {
    OpampClient.Callback callback = mock();
    OpampClientState state = OpampClientState.create();
    state.agentDescriptionState.set(createAgentDescriptionWithServiceName("startTest"));
    AgentToServerVisitor descriptionVisitor =
        AgentDescriptionVisitor.create(state.agentDescriptionState);
    AgentToServerVisitor mockVisitor = mock();
    ArgumentCaptor<RequestContext> contextCaptor = ArgumentCaptor.forClass(RequestContext.class);

    Message message =
        buildClient(callback, createVisitorsWith(descriptionVisitor, mockVisitor)).buildMessage();

    verify(mockVisitor).visit(contextCaptor.capture(), notNull());
    RequestContext requestContext = contextCaptor.getValue();
    assertThat(requestContext.stop).isFalse();
    assertThat(requestContext.disableCompression).isFalse();
    assertEquals(
        "startTest",
        message
            .agentToServer
            .getAgentDescription()
            .getIdentifyingAttributes(0)
            .getValue()
            .getStringValue());
  }

  @Test
  void verifyMessageBuildingAfterStopIsCalled() {
    AgentToServerVisitor visitor = mock();
    OpampClientImpl client = buildClient(null, createVisitorsWith(visitor));
    ArgumentCaptor<RequestContext> captor = ArgumentCaptor.forClass(RequestContext.class);
    client.stop();

    client.buildMessage();

    verify(scheduler).scheduleNow();
    verify(visitor).visit(captor.capture(), notNull());
    RequestContext context = captor.getValue();
    assertThat(context.stop).isTrue();
  }

  @Test
  void onResponse_withNotChangesToReport_doNotNotifyCallback() {
    OpampClient.Callback callback = mock();

    buildClient(callback).handleResponse(Opamp.ServerToAgent.getDefaultInstance());

    verifyNoInteractions(callback);
  }

  @Test
  void onResponse_withRemoteConfigStatusUpdate_notifyServerImmediately() {
    Opamp.ServerToAgent response =
        Opamp.ServerToAgent.newBuilder()
            .setRemoteConfig(
                Opamp.AgentRemoteConfig.newBuilder().setConfig(getAgentConfigMap("fileName", "{}")))
            .build();
    OpampClientImpl client =
        buildClient(
            new OpampClient.Callback() {
              @Override
              public void onMessage(OpampClient client, Response response) {
                client.setRemoteConfigStatus(
                    Opamp.RemoteConfigStatus.newBuilder()
                        .setStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING)
                        .build());
              }
            });

    client.handleResponse(response);

    verify(scheduler).scheduleNow();
  }

  private Opamp.AgentConfigMap getAgentConfigMap(String configFileName, String content) {
    Opamp.AgentConfigMap.Builder builder = Opamp.AgentConfigMap.newBuilder();
    builder.putConfigMap(
        configFileName,
        Opamp.AgentConfigFile.newBuilder().setBody(ByteString.copyFromUtf8(content)).build());
    return builder.build();
  }

  private OpampClientVisitors createVisitorsWith(AgentToServerVisitor... visitors) {
    OpampClientVisitors clientVisitors = mock();
    doReturn(Arrays.asList(visitors)).when(clientVisitors).asList();
    return clientVisitors;
  }

  private Opamp.AgentDescription createAgentDescriptionWithServiceName(String serviceName) {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("service.name", serviceName);
    return createAgentDescription(attributes);
  }

  private Opamp.AgentDescription createAgentDescription(Map<String, String> identifyingAttributes) {
    List<Anyvalue.KeyValue> keyValues = new ArrayList<>();
    identifyingAttributes.forEach((key, value) -> keyValues.add(createKeyValue(key, value)));
    return Opamp.AgentDescription.newBuilder().addAllIdentifyingAttributes(keyValues).build();
  }

  private static Anyvalue.KeyValue createKeyValue(String key, String value) {
    return Anyvalue.KeyValue.newBuilder()
        .setKey(key)
        .setValue(Anyvalue.AnyValue.newBuilder().setStringValue(value).build())
        .build();
  }

  private OpampClientImpl buildClient(OpampClient.Callback callback) {
    return buildClient(callback, mock());
  }

  private OpampClientImpl buildClient(OpampClient.Callback callback, OpampClientVisitors visitors) {
    return buildClient(callback, visitors, OpampClientState.create());
  }

  private OpampClientImpl buildClient(
      OpampClient.Callback callback, OpampClientVisitors visitors, OpampClientState state) {
    return OpampClientImpl.create(
        scheduler, RequestContext.newBuilder(), visitors, state, callback);
  }
}
