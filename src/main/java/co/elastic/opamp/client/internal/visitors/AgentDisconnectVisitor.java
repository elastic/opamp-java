package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.ClientContext;
import opamp.proto.Opamp;

public class AgentDisconnectVisitor implements AgentToServerVisitor {

  @Override
  public void visit(ClientContext clientContext, Opamp.AgentToServer.Builder builder) {
    builder.setAgentDisconnect(Opamp.AgentDisconnect.newBuilder().build());
  }
}
