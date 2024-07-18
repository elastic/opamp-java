package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import co.elastic.opamp.client.internal.state.observer.Observable;
import co.elastic.opamp.client.internal.state.observer.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import opamp.proto.Opamp;

/** Utility for values that can be omitted once they've been previously sent. */
public abstract class CompressableAgentToServerVisitor implements AgentToServerVisitor, Observer {
  private final AtomicBoolean alreadySent = new AtomicBoolean(false);

  protected abstract void doVisit(
      RequestContext requestContext, Opamp.AgentToServer.Builder builder);

  @Override
  public final void update(Observable observable) {
    alreadySent.set(false);
  }

  @Override
  public final void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    if (!alreadySent.getAndSet(true) || requestContext.disableCompression) {
      doVisit(requestContext, builder);
    }
  }
}
