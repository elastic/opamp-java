package io.opentelemetry.opamp.client.visitors;

import opamp.proto.Opamp;

public class RemoteConfigStatusVisitor implements AgentToServerVisitor {
  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {}
}
