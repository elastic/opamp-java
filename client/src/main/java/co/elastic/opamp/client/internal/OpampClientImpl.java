package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.dispatcher.Message;
import co.elastic.opamp.client.internal.dispatcher.MessageBuilder;
import co.elastic.opamp.client.internal.dispatcher.MessageDispatcher;
import co.elastic.opamp.client.internal.dispatcher.ResponseHandler;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.tools.ResponseActionsWatcher;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.response.Response;
import java.util.concurrent.TimeUnit;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, MessageBuilder, ResponseHandler {
  private final MessageDispatcher dispatcher;
  private final RequestContext.Builder contextBuilder;
  private final OpampClientVisitors visitors;
  private final OpampClientState state;
  private final Callback callback;

  public static OpampClientImpl create(
      MessageDispatcher dispatcher,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      OpampClientState state,
      Callback callback) {
    OpampClientImpl client =
        new OpampClientImpl(dispatcher, contextBuilder, visitors, state, callback);
    dispatcher.setMessageBuilder(client);
    dispatcher.setResponseHandler(client);
    return client;
  }

  private OpampClientImpl(
      MessageDispatcher dispatcher,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      OpampClientState state,
      Callback callback) {
    this.dispatcher = dispatcher;
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
  public void handleSuccess(Opamp.ServerToAgent serverToAgent) {
    state.sequenceNumberState.increment();
    callback.onConnect(this);
    if (serverToAgent == null) {
      return;
    }
    if (serverToAgent.hasErrorResponse()) {
      callback.onErrorResponse(this, serverToAgent.getErrorResponse());
    }
    long reportFullState = Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE;
    if ((serverToAgent.getFlags() & reportFullState) == reportFullState) {
      contextBuilder.disableCompression();
    }

    boolean notifyOnMessage = false;
    Response.Builder messageBuilder = Response.builder();

    if (serverToAgent.hasRemoteConfig()) {
      notifyOnMessage = true;
      messageBuilder.setRemoteConfig(serverToAgent.getRemoteConfig());
    }

    if (notifyOnMessage) {
      try (ResponseActionsWatcher watcher = ResponseActionsWatcher.create(serverToAgent, state)) {
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
  public void handleError(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
  }

  @Override
  public Message buildMessage() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    RequestContext requestContext = contextBuilder.buildAndReset();
    visitors.asList().forEach(visitor -> visitor.visit(requestContext, builder));
    return new Message(builder.build());
  }

  private void scheduleNow() {
    dispatcher.dispatchNow();
  }

  private void scheduleWithDelay() {
    dispatcher.dispatchWithDelay(30, TimeUnit.SECONDS);
  }
}
