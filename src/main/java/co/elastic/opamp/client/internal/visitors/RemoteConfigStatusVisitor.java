package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.ClientContext;
import opamp.proto.Opamp;

public class RemoteConfigStatusVisitor implements AgentToServerVisitor {

  @Override
  public void visit(ClientContext clientContext, Opamp.AgentToServer.Builder builder) {
    Opamp.RemoteConfigStatus status =
        Opamp.RemoteConfigStatus.newBuilder()
            .setStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_UNSET)
            .build();

    builder.setRemoteConfigStatus(status);
  }
}
