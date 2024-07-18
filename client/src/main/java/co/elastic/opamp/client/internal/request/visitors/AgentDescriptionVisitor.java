package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import co.elastic.opamp.client.internal.state.AgentDescriptionState;
import opamp.proto.Opamp;

public final class AgentDescriptionVisitor extends CompressableAgentToServerVisitor {
  private final AgentDescriptionState agentDescriptionState;

  public static AgentDescriptionVisitor create(AgentDescriptionState agentDescriptionState) {
    AgentDescriptionVisitor visitor = new AgentDescriptionVisitor(agentDescriptionState);
    agentDescriptionState.addObserver(visitor);
    return visitor;
  }

  private AgentDescriptionVisitor(AgentDescriptionState agentDescriptionState) {
    this.agentDescriptionState = agentDescriptionState;
  }

  @Override
  public void doVisit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setAgentDescription(agentDescriptionState.get());
  }
}
