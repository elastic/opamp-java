package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.request.HttpService;
import co.elastic.opamp.client.response.MessageData;
import java.io.IOException;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient {
  private final HttpService service;
  private final RequestContext.Builder contextBuilder;
  private final OpampClientVisitors visitors;
  private final Callback callback;

  OpampClientImpl(
      HttpService service,
      RequestContext.Builder contextBuilder,
      OpampClientVisitors visitors,
      Callback callback) {
    this.service = service;
    this.contextBuilder = contextBuilder;
    this.visitors = visitors;
    this.callback = callback;
  }

  @Override
  public void start() {
    sendMessage();
  }

  @Override
  public void stop() {
    contextBuilder.stop();
    sendMessage();
  }

  private void handleResponse(Opamp.ServerToAgent serverToAgent) {
    if (serverToAgent == null) {
      return;
    }
    MessageData.Builder messageBuilder = MessageData.builder();

    if (serverToAgent.hasRemoteConfig()) {
      messageBuilder.setRemoteConfig(serverToAgent.getRemoteConfig());
    }

    callback.onMessage(this, messageBuilder.build());
  }

  private Opamp.AgentToServer buildMessage(RequestContext requestContext) {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    visitors.asList().forEach(visitor -> visitor.visit(requestContext, builder));
    return builder.build();
  }

  void sendMessage() {
    try {
      Opamp.ServerToAgent serverToAgent =
          service.sendMessage(buildMessage(contextBuilder.buildAndReset()));
      handleResponse(serverToAgent);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
