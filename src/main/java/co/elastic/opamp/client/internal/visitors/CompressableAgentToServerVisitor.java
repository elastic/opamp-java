package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.RequestContext;
import co.elastic.opamp.client.internal.state.Observable;
import co.elastic.opamp.client.internal.state.Observer;
import opamp.proto.Opamp;

public abstract class CompressableAgentToServerVisitor implements AgentToServerVisitor, Observer {

  protected abstract void doVisit(
      RequestContext requestContext, Opamp.AgentToServer.Builder builder);

  @Override
  public final void update(Observable observable) {
    // todo ensure doVisit gets called next time
  }

  @Override
  public final void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    // todo check if it can call doVisit
  }
}
