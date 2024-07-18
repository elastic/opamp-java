package co.elastic.opamp.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.request.RequestBuilder;
import co.elastic.opamp.client.internal.request.RequestDispatcher;
import co.elastic.opamp.client.internal.state.AgentDescriptionState;
import co.elastic.opamp.client.internal.state.CapabilitiesState;
import co.elastic.opamp.client.internal.state.EffectiveConfigState;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.RemoteConfigStatusState;
import co.elastic.opamp.client.internal.state.SequenceNumberState;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.request.Schedule;
import co.elastic.opamp.client.response.Response;
import com.google.protobuf.ByteString;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class OpampClientImplTest {
  private RequestSender sender;
  private RequestDispatcher scheduler;
  private Schedule pollingSchedule;
  private RequestBuilder requestBuilder;

  @BeforeEach
  void setUp() {
    sender = mock();
    scheduler = mock();
    pollingSchedule = mock();
    requestBuilder = mock();
  }

  @Test
  void verifyStart() {
    RemoteConfigStatusState remoteConfigStatusState = mock();
    SequenceNumberState sequenceNumberState = mock();
    AgentDescriptionState agentDescriptionState = mock();
    EffectiveConfigState effectiveConfigState = mock();
    CapabilitiesState capabilitiesState = mock();
    OpampClientState state =
        new OpampClientState(
            remoteConfigStatusState,
            sequenceNumberState,
            agentDescriptionState,
            effectiveConfigState,
            capabilitiesState);
    OpampClientImpl client = buildClient(state);

    client.start();

    verify(scheduler).start(client);
    verify(remoteConfigStatusState).addObserver(client);
    verify(agentDescriptionState).addObserver(client);
    verify(effectiveConfigState).addObserver(client);
    verify(capabilitiesState).addObserver(client);
    verifyNoInteractions(sequenceNumberState);
  }

  @Test
  void verifyRequestBuildingAfterStopIsCalled() {
    OpampClientImpl client = buildClient();

    client.stop();

    InOrder inOrder = inOrder(requestBuilder, scheduler);
    inOrder.verify(requestBuilder).stop();
    inOrder.verify(scheduler).stop();
  }

  @Test
  void onResponse_withNoChangesToReport_doNotNotifyCallbackOnMessage() {
    OpampClient.Callback callback = mock();

    buildClient(callback).onSuccess(Opamp.ServerToAgent.getDefaultInstance());

    verify(callback, never()).onMessage(any(), any());
    verify(pollingSchedule).start();
    verifyNoMoreInteractions(pollingSchedule);
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

    client.start();
    client.onSuccess(response);

    InOrder inOrder = inOrder(pollingSchedule);
    inOrder.verify(pollingSchedule).start();
    inOrder.verify(pollingSchedule).fastForward();
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
    client.start();
    client.onSuccess(response);

    verify(pollingSchedule).start();
    verifyNoMoreInteractions(pollingSchedule);
  }

  @Test
  void onResponse_onConnectSuccess_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    client.onSuccess(Opamp.ServerToAgent.getDefaultInstance());

    verify(callback).onConnect(client);
    verify(callback, never()).onConnectFailed(any(), any());
  }

  @Test
  void onResponse_onConnectSuccess_withError_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    Opamp.ServerErrorResponse errorResponse = Opamp.ServerErrorResponse.getDefaultInstance();
    client.onSuccess(Opamp.ServerToAgent.newBuilder().setErrorResponse(errorResponse).build());

    verify(callback).onErrorResponse(client, errorResponse);
    verify(callback, never()).onMessage(any(), any());
  }

  @Test
  void onResponse_onConnectFailure_notifyCallback() {
    OpampClient.Callback callback = mock();
    OpampClientImpl client = buildClient(callback);
    Throwable throwable = mock();
    client.onError(throwable);

    verify(callback).onConnectFailed(client, throwable);
    verify(callback, never()).onConnect(any());
  }

  @Test
  void verifyDisableCompressionWhenRequestedByServer() {
    Opamp.ServerToAgent serverToAgent =
        Opamp.ServerToAgent.newBuilder()
            .setFlags(Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE)
            .build();
    OpampClientImpl client = buildClient();

    client.onSuccess(serverToAgent);

    verify(requestBuilder).disableCompression();
  }

  @Test
  void verifySequenceNumberIncreasesOnServerResponseReceived() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    assertThat(state.sequenceNumberState.get()).isEqualTo(1);

    client.onSuccess(Opamp.ServerToAgent.getDefaultInstance());

    assertThat(state.sequenceNumberState.get()).isEqualTo(2);
  }

  @Test
  void verifySequenceNumberDoesNotIncreaseOnRequestError() {
    OpampClientState state = OpampClientState.create();
    OpampClientImpl client = buildClient(state);
    assertThat(state.sequenceNumberState.get()).isEqualTo(1);

    client.onError(mock());

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

    verify(pollingSchedule).fastForward();
  }

  @Test
  void whenStatusIsNotUpdated_doNotNotifyServerImmediately() {
    OpampClientImpl client = buildClient();
    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));
    client.start();

    client.setRemoteConfigStatus(
        getRemoteConfigStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLYING));

    verify(pollingSchedule, never()).fastForward();
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
    return new OpampClientImpl(sender, scheduler, requestBuilder, pollingSchedule, state, callback);
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
