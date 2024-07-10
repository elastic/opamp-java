package io.opentelemetry.opamp.client.visitors;

import opamp.proto.Opamp;

public class EffectiveConfigVisitor implements AgentToServerVisitor {
  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {}
}
