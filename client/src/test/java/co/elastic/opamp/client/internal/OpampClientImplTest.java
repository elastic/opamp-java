package co.elastic.opamp.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpampClientImplTest {
  @Mock private RequestSender sender;
  @Mock private RequestDispatcher dispatcher;
  @Mock private RequestBuilder requestBuilder;
  @Mock private OpampClient.Callback callback;
  @Mock private RemoteConfigStatusState remoteConfigStatusState;
  @Mock private SequenceNumberState sequenceNumberState;
  @Mock private AgentDescriptionState agentDescriptionState;
  @Mock private EffectiveConfigState effectiveConfigState;
  @Mock private CapabilitiesState capabilitiesState;
  @Mock private InstanceUidState instanceUidState;
  @InjectMocks private OpampClientState mockState;

  @Test
  void verifyStart() {
    OpampClientImpl client = buildClient(mockState);

    client.start(callback);

    verify(dispatcher).start(client);
    verify(remoteConfigStatusState).addObserver(client);
    verify(agentDescriptionState).addObserver(client);
    verify(effectiveConfigState).addObserver(client);
    verify(capabilitiesState).addObserver(client);
    verify(instanceUidState).addObserver(client);
    verifyNoInteractions(sequenceNumberState);
  }

  @Test
  void verifyStartOnlyOnce() {
    OpampClientImpl client = buildClient();

    client.start(callback);

    try {
      client.start(callback);
      fail("Should have thrown an exception");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("The client has already been started");
    }
  }

  @Test
  void verifyStopOnlyOnce() {
    OpampClientImpl client = buildClient();
    client.start(callback);

    client.stop();

    try {
      client.stop();
      fail("Should have thrown an exception");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("The client has already been stopped");
    }
  }

  @Test
  void verifyStopOnlyAfterStart() {
    OpampClientImpl client = buildClient();

    try {
      client.stop();
      fail("Should have thrown an exception");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("The client has not been started");
    }
  }

  @Test
  void verifyRequestBuildingAfterStopIsCalled() {
    OpampClientImpl client = buildClient();
    client.start(callback);

    client.stop();

    InOrder inOrder = inOrder(requestBuilder, dispatcher);
    inOrder.verify(requestBuilder).stop();
    inOrder.verify(dispatcher).stop();
  }

  @Test
  void onSuccess_withNoChangesToReport_doNotNotifyCallbackOnMessage() {
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());
    OpampClientImpl client = buildClient();
    client.start(callback);

    client.run();

    verify(callback, never()).onMessage(any(), any());
  }

  @Test
  void onSuccess_withRemoteConfigStatusUpdate_notifyServerImmediately() {
    Opamp.ServerToAgent response =
        Opamp.ServerToAgent.newBuilder()
            .setRemoteConfig(
                Opamp.AgentRemoteConfig.newBuilder().setConfig(getAgentConfigMap("fileName", "{}")))
            .build();
    TestCallback testCallback =
        new TestCallback() {
          @Override
          public void onMessage(OpampClient client, MessageData messageData) {
            client.setRemoteConfigStatus(
                getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
          }
        };
    OpampClientImpl client = buildClient();
    client.start(testCallback);
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
    TestCallback testCallback =
        new TestCallback() {
          @Override
          public void onMessage(OpampClient client, MessageData messageData) {
            client.setRemoteConfigStatus(
                getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
          }
        };
    OpampClientImpl client = buildClient();
    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
    client.start(testCallback);
    prepareSuccessResponse(response);

    client.run();

    verify(dispatcher, never()).tryDispatchNow();
  }

  @Test
  void onSuccess_notifyCallback() {
    OpampClientImpl client = buildClient();
    client.start(callback);
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());

    client.run();

    verify(callback).onConnect(client);
    verify(callback, never()).onConnectFailed(any(), any());
  }

  @Test
  void onSuccess_whenRetryIsEnabled_disableRetry() {
    OpampClientImpl client = buildClient();
    client.start(callback);
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());
    doReturn(true).when(dispatcher).isRetryModeEnabled();

    client.run();

    verify(dispatcher).disableRetryMode();
  }

  @Test
  void onSuccess_withServerErrorData_notifyCallback() {
    OpampClientImpl client = buildClient();
    client.start(callback);
    Opamp.ServerErrorResponse errorResponse = Opamp.ServerErrorResponse.getDefaultInstance();
    prepareSuccessResponse(
        Opamp.ServerToAgent.newBuilder().setErrorResponse(errorResponse).build());

    client.run();

    verify(callback).onErrorResponse(client, errorResponse);
    verify(callback, never()).onMessage(any(), any());
  }

  @Test
  void onSuccess_withServerErrorData_withRetryInfo_enableRetryWithSuggestedInterval() {
    OpampClientImpl client = buildClient();
    client.start(callback);
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
    OpampClientImpl client = buildClient();
    client.start(callback);
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
    OpampClientImpl client = buildClient();
    client.start(callback);
    Throwable throwable = mock();
    prepareErrorResponse(throwable);

    client.run();

    verify(callback).onConnectFailed(client, throwable);
    verify(callback, never()).onConnect(any());
  }

  @Test
  void onError_enableRetry() {
    OpampClientImpl client = buildClient();
    client.start(callback);
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
    client.start(callback);
    prepareSuccessResponse(serverToAgent);

    client.run();

    verify(requestBuilder).disableCompression();
  }

  @Test
  void verifySequenceNumberIncreasesOnServerResponseReceived() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    client.start(callback);
    assertThat(state.sequenceNumberState.get()).isEqualTo(1);
    prepareSuccessResponse(Opamp.ServerToAgent.getDefaultInstance());

    client.run();

    assertThat(state.sequenceNumberState.get()).isEqualTo(2);
  }

  @Test
  void verifySequenceNumberDoesNotIncreaseOnRequestError() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    client.start(callback);
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
    client.start(callback);

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));

    verify(dispatcher).tryDispatchNow();
  }

  @Test
  void whenStatusIsNotUpdated_doNotNotifyServerImmediately() {
    OpampClientImpl client = buildClient();
    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
    client.start(callback);

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));

    verify(dispatcher, never()).tryDispatchNow();
  }

  @Test
  void whenServerProvidesNewInstanceUid_useIt() {
    OpampClientImpl client = buildClient(mockState);
    client.start(callback);
    byte[] serverProvidedUid = new byte[] {1, 2, 3};
    Opamp.ServerToAgent response =
        Opamp.ServerToAgent.newBuilder()
            .setAgentIdentification(
                Opamp.AgentIdentification.newBuilder()
                    .setNewInstanceUid(ByteString.copyFrom(serverProvidedUid))
                    .build())
            .build();
    prepareSuccessResponse(response);

    client.run();

    verify(instanceUidState).set(serverProvidedUid);
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
    return buildClient(OpampClientState.create());
  }

  private OpampClientImpl buildClient(OpampClientState state) {
    return new OpampClientImpl(sender, dispatcher, requestBuilder, state);
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
