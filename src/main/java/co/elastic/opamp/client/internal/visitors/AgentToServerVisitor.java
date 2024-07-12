package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.RequestContext;
import opamp.proto.Opamp;

public interface AgentToServerVisitor {
  void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder);
}
