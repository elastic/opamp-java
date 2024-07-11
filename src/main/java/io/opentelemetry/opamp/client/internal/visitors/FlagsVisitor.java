package io.opentelemetry.opamp.client.internal.visitors;

import opamp.proto.Opamp;

public class FlagsVisitor implements AgentToServerVisitor {

  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {
    builder.setFlags(Opamp.AgentToServerFlags.AgentToServerFlags_Unspecified_VALUE);
  }
}
