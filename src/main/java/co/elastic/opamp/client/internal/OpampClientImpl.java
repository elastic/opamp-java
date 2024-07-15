package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.scheduler.Message;
import co.elastic.opamp.client.internal.scheduler.MessageBuilder;
import co.elastic.opamp.client.internal.scheduler.MessageScheduler;
import co.elastic.opamp.client.internal.scheduler.ResponseHandler;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.tools.ResponseActionsWatcher;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.response.Response;
import java.util.concurrent.TimeUnit;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, MessageBuilder, ResponseHandler {
  private final MessageScheduler scheduler;
  private final RequestContext.Builder contextBuilder;
  private final OpampClientVisitors visitors;
  private final OpampClientState state;
  private final Callback callback;

  public static OpampClientImpl create(
      MessageScheduler messageScheduler,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      OpampClientState state,
      Callback callback) {
    OpampClientImpl client =
        new OpampClientImpl(messageScheduler, contextBuilder, visitors, state, callback);
    messageScheduler.setMessageBuilder(client);
    messageScheduler.setResponseHandler(client);
    return client;
  }

  private OpampClientImpl(
      MessageScheduler scheduler,
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
  public void handleSuccess(Opamp.ServerToAgent serverToAgent) {
    if (serverToAgent == null) {
      return;
    }
    boolean notifyCallback = false;
    Response.Builder messageBuilder = Response.builder();

    if (serverToAgent.hasRemoteConfig()) {
      notifyCallback = true;
      messageBuilder.setRemoteConfig(serverToAgent.getRemoteConfig());
    }

    if (notifyCallback) {
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
  public void handleError(Throwable throwable) {}

  @Override
  public Message buildMessage() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    RequestContext requestContext = contextBuilder.buildAndReset();
    visitors.asList().forEach(visitor -> visitor.visit(requestContext, builder));
    return new Message(builder.build());
  }

  private void scheduleNow() {
    scheduler.scheduleNow();
  }

  private void scheduleWithDelay() {
    scheduler.scheduleWithDelay(30, TimeUnit.SECONDS);
  }
}
