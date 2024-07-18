package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import co.elastic.opamp.client.internal.state.RemoteConfigStatusState;
import opamp.proto.Opamp;

public class RemoteConfigStatusVisitor extends CompressableAgentToServerVisitor {
  private final RemoteConfigStatusState remoteConfigStatusState;

  public static RemoteConfigStatusVisitor create(RemoteConfigStatusState remoteConfigStatusState) {
    RemoteConfigStatusVisitor visitor = new RemoteConfigStatusVisitor(remoteConfigStatusState);
    remoteConfigStatusState.addObserver(visitor);
    return visitor;
  }

  private RemoteConfigStatusVisitor(RemoteConfigStatusState remoteConfigStatusState) {
    this.remoteConfigStatusState = remoteConfigStatusState;
  }

  @Override
  protected void doVisit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setRemoteConfigStatus(remoteConfigStatusState.get());
  }
}
