package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.request.RequestBuilder;
import co.elastic.opamp.client.internal.request.RequestScheduler;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.tools.ResponseActionsWatcher;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.response.Response;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, Runnable, RequestSender.Callback {
  private final RequestSender sender;
  private final RequestScheduler scheduler;
  private final RequestBuilder requestBuilder;
  private final OpampClientState state;
  private final Callback callback;

  public static OpampClientImpl create(
      RequestSender sender,
      OpampClientVisitors visitors,
      OpampClientState state,
      Callback callback) {
    RequestBuilder requestBuilder = RequestBuilder.create(visitors);
    RequestScheduler scheduler = RequestScheduler.create();
    return new OpampClientImpl(sender, scheduler, requestBuilder, state, callback);
  }

  OpampClientImpl(
      RequestSender sender,
      RequestScheduler scheduler,
      RequestBuilder requestBuilder,
      OpampClientState state,
      Callback callback) {
    this.sender = sender;
    this.scheduler = scheduler;
    this.requestBuilder = requestBuilder;
    this.state = state;
    this.callback = callback;
  }

  @Override
  public void start() {
    scheduler.start(this);
  }

  @Override
  public void stop() {
    requestBuilder.stop();
    scheduler.scheduleImmediatelyAndStop();
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
  public void onSuccess(Opamp.ServerToAgent response) {
    state.sequenceNumberState.increment();
    callback.onConnect(this);
    if (response == null) {
      return;
    }
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
      try (ResponseActionsWatcher watcher = ResponseActionsWatcher.create(response, state)) {
        callback.onMessage(this, messageBuilder.build());
        if (watcher.stateHasChanged()) {
          scheduler.scheduleImmediately();
          return;
        }
      }
    }

    scheduler.scheduleNext();
  }

  @Override
  public void onError(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
  }

  @Override
  public void run() {
    Request request = requestBuilder.buildAndReset();
    sender.send(request.getAgentToServer(), this);
  }
}
