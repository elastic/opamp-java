package co.elastic.opamp.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.request.RequestBuilder;
import co.elastic.opamp.client.internal.request.RequestDispatcher;
import co.elastic.opamp.client.internal.state.AgentDescriptionState;
import co.elastic.opamp.client.internal.state.CapabilitiesState;
import co.elastic.opamp.client.internal.state.EffectiveConfigState;
import co.elastic.opamp.client.internal.state.InstanceUidState;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.RemoteConfigStatusState;
import co.elastic.opamp.client.internal.state.SequenceNumberState;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.response.MessageData;
import com.google.protobuf.ByteString;
import java.time.Duration;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class OpampClientImplTest {
  private RequestSender sender;
  private RequestDispatcher dispatcher;
  private RequestBuilder requestBuilder;

  @BeforeEach
  void setUp() {
    sender = mock();
    dispatcher = mock();
    requestBuilder = mock();
  }

  @Test
  void verifyStart() {
    RemoteConfigStatusState remoteConfigStatusState = mock();
    SequenceNumberState sequenceNumberState = mock();
    AgentDescriptionState agentDescriptionState = mock();
    EffectiveConfigState effectiveConfigState = mock();
    CapabilitiesState capabilitiesState = mock();
    InstanceUidState instanceUidState = mock();
    OpampClientState state =
        new OpampClientState(
            remoteConfigStatusState,
            sequenceNumberState,
            agentDescriptionState,
            effectiveConfigState,
            capabilitiesState,
            instanceUidState);
    OpampClientImpl client = buildClient(state);

    client.start();

    verify(dispatcher).start(client);
    verify(remoteConfigStatusState).addObserver(client);
    verify(agentDescriptionState).addObserver(client);
    verify(effectiveConfigState).addObserver(client);
    verify(capabilitiesState).addObserver(client);
    verify(instanceUidState).addObserver(client);
    verifyNoInteractions(sequenceNumberState);
  }

  @Test
  void verifyRequestBuildingAfterStopIsCalled() {
    OpampClientImpl client = buildClient();

    client.stop();

    InOrder inOrder = inOrder(requestBuilder, dispatcher);
    inOrder.verify(requestBuilder).stop();
    inOrder.verify(dispatcher).stop();
  }

  @Test
  void onSuccess_withNoChangesToReport_doNotNotifyCallbackOnMessage() {
    OpampClient.Callback callback = mock();
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());

    buildClient(callback).run();

    verify(callback, never()).onMessage(any(), any());
  }

  @Test
  void onSuccess_withRemoteConfigStatusUpdate_notifyServerImmediately() {
    Opamp.ServerToAgent response =
        Opamp.ServerToAgent.newBuilder()
            .setRemoteConfig(
                Opamp.AgentRemoteConfig.newBuilder().setConfig(getAgentConfigMap("fileName", "{}")))
            .build();
    OpampClientImpl client =
        buildClient(
            new TestCallback() {
              @Override
              public void onMessage(OpampClient client, MessageData messageData) {
                client.setRemoteConfigStatus(
                    getRemoteConfigStatus(
                        Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
              }
            });

    client.start();
    prepareSuccessResponse(response);
    client.run();

    verify(dispatcher).tryDispatchNow();
  }

  @Test
  void onSuccess_withRemoteConfigStatus_withoutChange_doNotNotifyServerImmediately() {
    Opamp.ServerToAgent response =
        Opamp.ServerToAgent.newBuilder()
            .setRemoteConfig(
                Opamp.AgentRemoteConfig.newBuilder().setConfig(getAgentConfigMap("fileName", "{}")))
            .build();
    OpampClientImpl client =
        buildClient(
            new TestCallback() {
              @Override
              public void onMessage(OpampClient client, MessageData messageData) {
                client.setRemoteConfigStatus(
                    getRemoteConfigStatus(
                        Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
              }
            });

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
    client.start();
    prepareSuccessResponse(response);
    client.run();
  }

  @Test
  void onSuccess_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());

    client.run();

    verify(callback).onConnect(client);
    verify(callback, never()).onConnectFailed(any(), any());
  }

  @Test
  void onSuccess_whenRetryIsEnabled_disableRetry() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());
    doReturn(true).when(dispatcher).isRetryModeEnabled();

    client.run();

    verify(dispatcher).disableRetryMode();
  }

  @Test
  void onSuccess_withServerErrorData_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    Opamp.ServerErrorResponse errorResponse = Opamp.ServerErrorResponse.getDefaultInstance();
    prepareSuccessResponse(
        Opamp.ServerToAgent.newBuilder().setErrorResponse(errorResponse).build());

    client.run();

    verify(callback).onErrorResponse(client, errorResponse);
    verify(callback, never()).onMessage(any(), any());
  }

  @Test
  void onSuccess_withServerErrorData_withRetryInfo_enableRetryWithSuggestedInterval() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    long retryAfterNanoseconds = 123;
    Opamp.ServerErrorResponse errorResponse =
        Opamp.ServerErrorResponse.newBuilder()
            .setType(Opamp.ServerErrorResponseType.ServerErrorResponseType_Unavailable)
            .setRetryInfo(
                Opamp.RetryInfo.newBuilder()
                    .setRetryAfterNanoseconds(retryAfterNanoseconds)
                    .build())
            .build();
    prepareSuccessResponse(
        Opamp.ServerToAgent.newBuilder().setErrorResponse(errorResponse).build());

    client.run();

    verify(dispatcher).enableRetryMode(Duration.ofNanos(retryAfterNanoseconds));
  }

  @Test
  void onSuccess_withUnavailableType_withoutRetryInfo_enableRetry() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    Opamp.ServerErrorResponse errorResponse =
        Opamp.ServerErrorResponse.newBuilder()
            .setType(Opamp.ServerErrorResponseType.ServerErrorResponseType_Unavailable)
            .build();
    prepareSuccessResponse(
        Opamp.ServerToAgent.newBuilder().setErrorResponse(errorResponse).build());

    client.run();

    verify(dispatcher).enableRetryMode(null);
  }

  @Test
  void onError_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    Throwable throwable = mock();
    prepareErrorResponse(throwable);

    client.run();

    verify(callback).onConnectFailed(client, throwable);
    verify(callback, never()).onConnect(any());
  }

  @Test
  void onError_enableRetry() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    prepareErrorResponse(mock());
    doReturn(false).when(dispatcher).isRetryModeEnabled();

    client.run();

    verify(dispatcher).enableRetryMode(null);
  }

  @Test
  void verifyDisableCompressionWhenRequestedByServer() {
    Opamp.ServerToAgent serverToAgent =
        Opamp.ServerToAgent.newBuilder()
            .setFlags(Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE)
            .build();
    OpampClientImpl client = buildClient();
    prepareSuccessResponse(serverToAgent);

    client.run();

    verify(requestBuilder).disableCompression();
  }

  @Test
  void verifySequenceNumberIncreasesOnServerResponseReceived() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    assertThat(state.sequenceNumberState.get()).isEqualTo(1);
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());

    client.run();

    assertThat(state.sequenceNumberState.get()).isEqualTo(2);
  }

  @Test
  void verifySequenceNumberDoesNotIncreaseOnRequestError() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    assertThat(state.sequenceNumberState.get()).isEqualTo(1);
    prepareErrorResponse(mock());

    client.run();

    assertThat(state.sequenceNumberState.get()).isEqualTo(1);
  }

  @Test
  void whenStatusIsUpdated_notifyServerImmediately() {
    OpampClientImpl client = buildClient();
    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_UNSET));
    client.start();

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));

    verify(dispatcher).tryDispatchNow();
  }

  @Test
  void whenStatusIsNotUpdated_doNotNotifyServerImmediately() {
    OpampClientImpl client = buildClient();
    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
    client.start();

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));

    verify(dispatcher, never()).tryDispatchNow();
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

  private OpampClientImpl buildClient() {
    return buildClient(mock(OpampClient.Callback.class));
  }

  private OpampClientImpl buildClient(OpampClientState state) {
    return buildClient(mock(), state);
  }

  private OpampClientImpl buildClient(OpampClient.Callback callback) {
    return buildClient(callback, OpampClientState.create());
  }

  private OpampClientImpl buildClient(OpampClient.Callback callback, OpampClientState state) {
    return new OpampClientImpl(sender, dispatcher, requestBuilder, state, callback);
  }

  private void prepareSuccessResponse(Opamp.ServerToAgent serverToAgent) {
    Request request = prepareRequestData();
    doReturn(RequestSender.Response.success(serverToAgent)).when(sender).send(request);
  }

  private void prepareErrorResponse(Throwable throwable) {
    Request request = prepareRequestData();
    doReturn(RequestSender.Response.error(throwable)).when(sender).send(request);
  }

  private Request prepareRequestData() {
    Request request = mock();
    doReturn(request).when(requestBuilder).buildAndReset();
    return request;
  }

  private static class TestCallback implements OpampClient.Callback {

    @Override
    public void onConnect(OpampClient client) {}

    @Override
    public void onConnectFailed(OpampClient client, Throwable throwable) {}

    @Override
    public void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse) {}

    @Override
    public void onMessage(OpampClient client, MessageData messageData) {}
  }
}
