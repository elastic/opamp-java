package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.dispatcher.Message;
import co.elastic.opamp.client.internal.dispatcher.MessageBuilder;
import co.elastic.opamp.client.internal.dispatcher.MessageDispatcher;
import co.elastic.opamp.client.internal.dispatcher.ResponseHandler;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.response.Response;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, MessageBuilder, ResponseHandler {
  private final MessageDispatcher dispatcher;
  private final RequestContext.Builder contextBuilder;
  private final OpampClientVisitors visitors;
  private final Callback callback;

  public static OpampClientImpl create(
      MessageDispatcher dispatcher,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      Callback callback) {
    OpampClientImpl client = new OpampClientImpl(dispatcher, contextBuilder, visitors, callback);
    dispatcher.setMessageBuilder(client);
    dispatcher.setResponseHandler(client);
    return client;
  }

  private OpampClientImpl(
      MessageDispatcher dispatcher,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      Callback callback) {
    this.dispatcher = dispatcher;
    this.contextBuilder = contextBuilder;
    this.visitors = visitors;
    this.callback = callback;
  }

  @Override
  public void start() {
    dispatcher.start();
  }

  @Override
  public void stop() {
    contextBuilder.stop();
  }

  @Override
  public void handleResponse(Opamp.ServerToAgent serverToAgent) {
    if (serverToAgent == null) {
      return;
    }
    Response.Builder messageBuilder = Response.builder();

    if (serverToAgent.hasRemoteConfig()) {
      messageBuilder.setRemoteConfig(serverToAgent.getRemoteConfig());
    }

    callback.onMessage(this, messageBuilder.build());
  }

  @Override
  public void handleError(Throwable t) {}

  @Override
  public Message buildMessage() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    RequestContext requestContext = contextBuilder.buildAndReset();
    visitors.asList().forEach(visitor -> visitor.visit(requestContext, builder));
    return new Message(builder.build());
  }
}
