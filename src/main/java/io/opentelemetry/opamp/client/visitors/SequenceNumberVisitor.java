package io.opentelemetry.opamp.client.visitors;

import opamp.proto.Opamp;

public class SequenceNumberVisitor implements AgentToServerVisitor {
  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {}
}
