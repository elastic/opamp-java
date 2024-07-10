package io.opentelemetry.opamp.client.visitors;

import opamp.proto.Opamp;

public interface AgentToServerVisitor {
  void visit(Opamp.AgentToServer.Builder builder);
}
