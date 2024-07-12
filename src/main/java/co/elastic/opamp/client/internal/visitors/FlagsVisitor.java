package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.RequestContext;
import opamp.proto.Opamp;

public class FlagsVisitor implements AgentToServerVisitor {

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setFlags(Opamp.AgentToServerFlags.AgentToServerFlags_Unspecified_VALUE);
  }
}
