package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.ClientContext;
import opamp.proto.Opamp;

public interface AgentToServerVisitor {
  void visit(ClientContext clientContext, Opamp.AgentToServer.Builder builder);
}
