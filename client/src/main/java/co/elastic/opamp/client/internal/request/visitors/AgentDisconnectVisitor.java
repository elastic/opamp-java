package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import opamp.proto.Opamp;

public final class AgentDisconnectVisitor implements AgentToServerVisitor {

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    if (requestContext.stop) builder.setAgentDisconnect(Opamp.AgentDisconnect.newBuilder().build());
  }
}
