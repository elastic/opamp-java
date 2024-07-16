package co.elastic.opamp.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.dispatcher.Message;
import co.elastic.opamp.client.internal.dispatcher.MessageDispatcher;
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
  private MessageDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    dispatcher = mock();
  }

  @Test
  void verifyStart() {
    buildClient().start();

    verify(dispatcher).dispatchNow();
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

    verify(dispatcher).dispatchNow();
    verify(visitor).visit(captor.capture(), notNull());
    RequestContext context = captor.getValue();
    assertThat(context.stop).isTrue();
  }

  @Test
  void onResponse_scheduleDelayedDispatch() {
    buildClient().handleSuccess(Opamp.ServerToAgent.getDefaultInstance());

    verify(dispatcher).dispatchWithDelay(anyLong(), any());
  }

  @Test
  void onResponse_withNotChangesToReport_doNotNotifyCallbackOnMessage() {
    OpampClient.Callback callback = mock();

    buildClient(callback).handleSuccess(Opamp.ServerToAgent.getDefaultInstance());

    verify(callback, never()).onMessage(any(), any());
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
            new TestCallback() {
              @Override
              public void onMessage(OpampClient client, Response response) {
                client.setRemoteConfigStatus(
                    getRemoteConfigStatus(
                        Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
              }
            });

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_UNSET));
    client.handleSuccess(response);

    verify(dispatcher).dispatchNow();
    verify(dispatcher, never()).dispatchWithDelay(anyLong(), any());
  }

  @Test
  void onResponse_withRemoteConfigStatus_withoutChange_doNotNotifyServerImmediately() {
    Opamp.ServerToAgent response =
        Opamp.ServerToAgent.newBuilder()
            .setRemoteConfig(
                Opamp.AgentRemoteConfig.newBuilder().setConfig(getAgentConfigMap("fileName", "{}")))
            .build();
    OpampClientImpl client =
        buildClient(
            new TestCallback() {
              @Override
              public void onMessage(OpampClient client, Response response) {
                client.setRemoteConfigStatus(
                    getRemoteConfigStatus(
                        Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
              }
            });

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
    client.handleSuccess(response);

    verify(dispatcher, never()).dispatchNow();
    verify(dispatcher).dispatchWithDelay(anyLong(), any());
  }

  @Test
  void onResponse_onConnectSuccess_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    client.handleSuccess(Opamp.ServerToAgent.getDefaultInstance());

    verify(callback).onConnect(client);
    verify(callback, never()).onConnectFailed(any(), any());
  }

  @Test
  void onResponse_onConnectSuccess_withError_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    Opamp.ServerErrorResponse errorResponse = Opamp.ServerErrorResponse.getDefaultInstance();
    client.handleSuccess(Opamp.ServerToAgent.newBuilder().setErrorResponse(errorResponse).build());

    verify(callback).onErrorResponse(client, errorResponse);
    verify(callback, never()).onMessage(any(), any());
  }

  @Test
  void onResponse_onConnectFailure_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    Throwable throwable = mock();
    client.handleError(throwable);

    verify(callback).onConnectFailed(client, throwable);
    verify(callback, never()).onConnect(any());
  }

  @Test
  void verifyDisableCompressionWhenRequestedByServer() {
    Opamp.ServerToAgent serverToAgent =
        Opamp.ServerToAgent.newBuilder()
            .setFlags(Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE)
            .build();
    RequestContext.Builder contextBuilder = mock();
    OpampClientImpl client = buildClient(contextBuilder);

    client.handleSuccess(serverToAgent);

    verify(contextBuilder).disableCompression();
  }

  @Test
  void verifySequenceNumberIncreasesOnServerResponseReceived() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    assertThat(state.sequenceNumberState.get()).isEqualTo(1);

    client.handleSuccess(Opamp.ServerToAgent.getDefaultInstance());

    assertThat(state.sequenceNumberState.get()).isEqualTo(2);
  }

  @Test
  void verifySequenceNumberDoesNotIncreaseOnRequestError() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    assertThat(state.sequenceNumberState.get()).isEqualTo(1);

    client.handleError(mock());

    assertThat(state.sequenceNumberState.get()).isEqualTo(1);
  }

  private static Opamp.RemoteConfigStatus getRemoteConfigStatus(Opamp.RemoteConfigStatuses status) {
    return Opamp.RemoteConfigStatus.newBuilder().setStatus(status).build();
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

  private OpampClientImpl buildClient() {
    return buildClient(mock(OpampClient.Callback.class));
  }

  private OpampClientImpl buildClient(OpampClientState state) {
    return buildClient(mock(), mock(), state);
  }

  private OpampClientImpl buildClient(OpampClient.Callback callback) {
    return buildClient(callback, mock());
  }

  private OpampClientImpl buildClient(OpampClient.Callback callback, OpampClientVisitors visitors) {
    return buildClient(callback, visitors, OpampClientState.create());
  }

  private OpampClientImpl buildClient(
      OpampClient.Callback callback, OpampClientVisitors visitors, OpampClientState state) {
    return buildClient(callback, visitors, state, RequestContext.newBuilder());
  }

  private OpampClientImpl buildClient(RequestContext.Builder contextBuilder) {
    return buildClient(mock(), mock(), OpampClientState.create(), contextBuilder);
  }

  private OpampClientImpl buildClient(
      OpampClient.Callback callback,
      OpampClientVisitors visitors,
      OpampClientState state,
      RequestContext.Builder contextBuilder) {
    return OpampClientImpl.create(dispatcher, contextBuilder, visitors, state, callback);
  }

  private static class TestCallback implements OpampClient.Callback {

    @Override
    public void onConnect(OpampClient client) {}

    @Override
    public void onConnectFailed(OpampClient client, Throwable throwable) {}

    @Override
    public void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse) {}

    @Override
    public void onMessage(OpampClient client, Response response) {}
  }
}
