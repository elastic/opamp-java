package io.opentelemetry.opamp.client;

import io.opentelemetry.opamp.client.request.Operation;
import io.opentelemetry.opamp.client.visitors.AgentDisconnectVisitor;
import io.opentelemetry.opamp.client.visitors.AgentToServerVisitor;
import java.io.IOException;
import java.util.List;
import opamp.proto.Opamp;

public class OpampClientImpl implements OpampClient {
  private final Operation operation;
  private final List<AgentToServerVisitor> constantVisitors;
  private final AgentDisconnectVisitor agentDisconnectVisitor;

  OpampClientImpl(
      Operation operation,
      List<AgentToServerVisitor> constantVisitors,
      AgentDisconnectVisitor agentDisconnectVisitor) {
    this.operation = operation;
    this.constantVisitors = constantVisitors;
    this.agentDisconnectVisitor = agentDisconnectVisitor;
  }

  @Override
  public void reportStatus() {
    Opamp.AgentToServer.Builder builder = getBuilder();
    send(builder.build());
  }

  @Override
  public void disconnect() {
    Opamp.AgentToServer.Builder builder = getBuilder();
    agentDisconnectVisitor.visit(builder);
    send(builder.build());
  }

  private Opamp.AgentToServer.Builder getBuilder() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    constantVisitors.forEach(visitor -> visitor.visit(builder));
    return builder;
  }

  private void send(Opamp.AgentToServer message) {
    try {
      operation.sendMessage(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
