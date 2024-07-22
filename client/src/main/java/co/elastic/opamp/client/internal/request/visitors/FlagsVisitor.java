package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import opamp.proto.Opamp;

public final class FlagsVisitor implements AgentToServerVisitor {

  public static FlagsVisitor create() {
    return new FlagsVisitor();
  }

  private FlagsVisitor() {}

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setFlags(Opamp.AgentToServerFlags.AgentToServerFlags_Unspecified_VALUE);
  }
}
