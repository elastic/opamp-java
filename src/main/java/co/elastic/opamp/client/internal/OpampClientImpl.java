package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.requests.HttpService;
import java.io.IOException;
import java.util.List;
import opamp.proto.Opamp;

public class OpampClientImpl implements OpampClient {
  private final HttpService service;
  private final RequestContext.Builder contextBuilder;
  private final List<AgentToServerVisitor> visitors;

  OpampClientImpl(
      HttpService service,
      RequestContext.Builder contextBuilder,
      List<AgentToServerVisitor> visitors) {
    this.service = service;
    this.contextBuilder = contextBuilder;
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

  private Opamp.AgentToServer buildMessage() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    visitors.forEach(visitor -> visitor.visit(contextBuilder.buildAndReset(), builder));
    return builder.build();
  }

  private void sendMessage() {
    try {
      Opamp.ServerToAgent serverToAgent = service.sendMessage(buildMessage());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
