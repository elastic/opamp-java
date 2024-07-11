package io.opentelemetry.opamp.client.internal.visitors;

import opamp.proto.Opamp;

public interface AgentToServerVisitor {
  void visit(Opamp.AgentToServer.Builder builder);
}
