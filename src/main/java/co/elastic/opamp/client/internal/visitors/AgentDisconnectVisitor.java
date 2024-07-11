package co.elastic.opamp.client.internal.visitors;

import opamp.proto.Opamp;

public class AgentDisconnectVisitor implements AgentToServerVisitor {

  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {
    builder.setAgentDisconnect(Opamp.AgentDisconnect.newBuilder().build());
  }
}
