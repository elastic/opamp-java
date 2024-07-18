package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.request.RequestBuilder;
import co.elastic.opamp.client.internal.request.RequestDispatcher;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.observer.Observable;
import co.elastic.opamp.client.internal.state.observer.Observer;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.request.schedule.IntervalSchedule;
import co.elastic.opamp.client.response.Response;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, Observer, Runnable {
  private final RequestSender sender;
  private final RequestDispatcher dispatcher;
  private final RequestBuilder requestBuilder;
  private final OpampClientState state;
  private final Callback callback;

  public static OpampClientImpl create(
      RequestSender sender,
      OpampClientVisitors visitors,
      OpampClientState state,
      IntervalSchedule pollingSchedule,
      IntervalSchedule retrySchedule,
      Callback callback) {
    RequestBuilder requestBuilder = RequestBuilder.create(visitors);
    RequestDispatcher dispatcher = RequestDispatcher.create(pollingSchedule, retrySchedule);
    return new OpampClientImpl(sender, dispatcher, requestBuilder, state, callback);
  }

  OpampClientImpl(
      RequestSender sender,
      RequestDispatcher dispatcher,
      RequestBuilder requestBuilder,
      OpampClientState state,
      Callback callback) {
    this.sender = sender;
    this.dispatcher = dispatcher;
    this.requestBuilder = requestBuilder;
    this.state = state;
    this.callback = callback;
  }

  @Override
  public void start() {
    observeStatusChange();
    dispatcher.start(this);
  }

  @Override
  public void stop() {
    requestBuilder.stop();
    dispatcher.stop();
  }

  @Override
  public void setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus) {
    state.remoteConfigStatusState.set(remoteConfigStatus);
  }

  @Override
  public void setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig) {
    state.effectiveConfigState.set(effectiveConfig);
  }

  @Override
  public void addCapabilities(long capabilities) {
    state.capabilitiesState.add(capabilities);
  }

  @Override
  public void removeCapabilities(long capabilities) {
    state.capabilitiesState.remove(capabilities);
  }

  private void onSuccess(Opamp.ServerToAgent response) {
    state.sequenceNumberState.increment();
    callback.onConnect(this);
    if (response == null) {
      return;
    }
    handleResponse(response);
  }

  private void handleResponse(Opamp.ServerToAgent response) {
    if (response.hasErrorResponse()) {
      callback.onErrorResponse(this, response.getErrorResponse());
    }
    long reportFullState = Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE;
    if ((response.getFlags() & reportFullState) == reportFullState) {
      requestBuilder.disableCompression();
    }

    boolean notifyOnMessage = false;
    Response.Builder messageBuilder = Response.builder();

    if (response.hasRemoteConfig()) {
      notifyOnMessage = true;
      messageBuilder.setRemoteConfig(response.getRemoteConfig());
    }

    if (notifyOnMessage) {
      callback.onMessage(this, messageBuilder.build());
    }
  }

  private void onError(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
  }

  @Override
  public void run() {
    Request request = requestBuilder.buildAndReset();

    RequestSender.Response response = sender.send(request.getAgentToServer());

    if (response instanceof RequestSender.Response.Success) {
      onSuccess(((RequestSender.Response.Success) response).data);
    } else if (response instanceof RequestSender.Response.Error) {
      onError(((RequestSender.Response.Error) response).throwable);
    } else {
      throw new IllegalStateException("Unexpected response: " + response);
    }
  }

  @Override
  public void update(Observable observable) {
    // There was an agent status change.
    dispatcher.tryDispatchNow();
  }

  private void observeStatusChange() {
    state.agentDescriptionState.addObserver(this);
    state.effectiveConfigState.addObserver(this);
    state.remoteConfigStatusState.addObserver(this);
    state.capabilitiesState.addObserver(this);
  }
}
