package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.request.HttpService;
import co.elastic.opamp.client.response.MessageData;
import java.io.IOException;
import java.util.List;
import opamp.proto.Opamp;

public class OpampClientImpl implements OpampClient {
  private final HttpService service;
  private final RequestContext.Builder contextBuilder;
  private final List<AgentToServerVisitor> visitors;
  private final Callback callback;

  OpampClientImpl(
      HttpService service,
      RequestContext.Builder contextBuilder,
      Callback callback,
      List<AgentToServerVisitor> visitors) {
    this.service = service;
    this.contextBuilder = contextBuilder;
    this.callback = callback;
    this.visitors = visitors;
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

  private Opamp.AgentToServer buildMessage() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    visitors.forEach(visitor -> visitor.visit(contextBuilder.buildAndReset(), builder));
    return builder.build();
  }

  void sendMessage() {
    try {
      Opamp.ServerToAgent serverToAgent = service.sendMessage(buildMessage());
      handleResponse(serverToAgent);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
