package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.RequestContext;
import opamp.proto.Opamp;

public class AgentDisconnectVisitor implements AgentToServerVisitor {

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    if (requestContext.stop) builder.setAgentDisconnect(Opamp.AgentDisconnect.newBuilder().build());
  }
}
