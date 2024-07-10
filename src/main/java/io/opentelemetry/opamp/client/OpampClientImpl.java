package io.opentelemetry.opamp.client;

import io.opentelemetry.opamp.client.request.Operation;
import io.opentelemetry.opamp.client.visitors.AgentToServerVisitor;
import java.io.IOException;
import java.util.List;
import opamp.proto.Opamp;

public class OpampClientImpl implements OpampClient {
  private final Operation operation;
  private final List<AgentToServerVisitor> visitors;

  public OpampClientImpl(Operation operation, List<AgentToServerVisitor> visitors) {
    this.operation = operation;
    this.visitors = visitors;
  }

  @Override
  public void reportStatus() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    visitors.forEach(visitor -> visitor.visit(builder));
    send(builder.build());
  }

  private void send(Opamp.AgentToServer message) {
    try {
      operation.sendMessage(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
