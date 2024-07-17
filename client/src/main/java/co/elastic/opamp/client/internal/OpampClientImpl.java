package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.dispatcher.RequestDispatcher;
import co.elastic.opamp.client.internal.dispatcher.Scheduler;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.tools.ResponseActionsWatcher;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.response.Response;
import java.util.concurrent.TimeUnit;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, RequestSender.Callback {
  private final Scheduler scheduler;
  private final RequestContext.Builder contextBuilder;
  private final OpampClientVisitors visitors;
  private final OpampClientState state;
  private final Callback callback;

  public static OpampClientImpl create(
      RequestSender sender,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      OpampClientState state,
      Callback callback) {
    RequestDispatcher dispatcher = new RequestDispatcher(sender);
    Scheduler scheduler = Scheduler.create(dispatcher);
    OpampClientImpl client =
        new OpampClientImpl(scheduler, contextBuilder, visitors, state, callback);
    dispatcher.setRequestCallback(client);
    dispatcher.setRequestSupplier(client::buildRequest);
    return client;
  }

  OpampClientImpl(
      Scheduler scheduler,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      OpampClientState state,
      Callback callback) {
    this.scheduler = scheduler;
    this.contextBuilder = contextBuilder;
    this.visitors = visitors;
    this.state = state;
    this.callback = callback;
  }

  @Override
  public void start() {
    scheduleNow();
  }

  @Override
  public void stop() {
    contextBuilder.stop();
    scheduleNow();
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
      contextBuilder.disableCompression();
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
          scheduleNow();
          return;
        }
      }
    }

    scheduleWithDelay();
  }

  @Override
  public void onError(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
  }

  public Request buildRequest() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    RequestContext requestContext = contextBuilder.buildAndReset();
    visitors.asList().forEach(visitor -> visitor.visit(requestContext, builder));
    return Request.create(builder.build());
  }

  private void scheduleNow() {
    scheduler.dispatchNow();
  }

  private void scheduleWithDelay() {
    scheduler.dispatchAfter(30, TimeUnit.SECONDS);
  }
}
