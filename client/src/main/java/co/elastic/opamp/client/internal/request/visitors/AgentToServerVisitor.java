package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import opamp.proto.Opamp;

/**
 * AgentToServer request builder visitor. Each implementation should match one of the AgentToServer
 * fields and ensure the field is added to a request when necessary.
 */
public interface AgentToServerVisitor {
  /**
   * Visits a request builder.
   *
   * @param requestContext The context of the request being build. Check {@link RequestContext} for
   *     more details.
   * @param builder The AgentToServer message builder.
   */
  void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder);
}
