package io.opentelemetry.opamp.client;

import io.opentelemetry.opamp.client.request.OpampService;
import io.opentelemetry.opamp.client.internal.visitors.AgentToServerVisitor;
import java.io.IOException;
import java.util.List;
import opamp.proto.Opamp;

public class OpampClientImpl implements OpampClient {
  private final OpampService service;
  private final List<AgentToServerVisitor> visitors;

  OpampClientImpl(OpampService service, List<AgentToServerVisitor> visitors) {
    this.service = service;
    this.visitors = visitors;
  }

  @Override
  public void start() {
    sendMessage();
  }

  @Override
  public void stop() {
    sendMessage();
  }

  private Opamp.AgentToServer buildMessage() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    visitors.forEach(visitor -> visitor.visit(builder));
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
