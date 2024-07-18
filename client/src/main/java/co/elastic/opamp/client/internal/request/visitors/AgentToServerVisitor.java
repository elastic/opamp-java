package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import opamp.proto.Opamp;

public interface AgentToServerVisitor {
  void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder);
}
