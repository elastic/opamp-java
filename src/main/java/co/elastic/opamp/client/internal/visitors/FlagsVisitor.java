package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.ClientContext;
import opamp.proto.Opamp;

public class FlagsVisitor implements AgentToServerVisitor {

  @Override
  public void visit(ClientContext clientContext, Opamp.AgentToServer.Builder builder) {
    builder.setFlags(Opamp.AgentToServerFlags.AgentToServerFlags_Unspecified_VALUE);
  }
}
