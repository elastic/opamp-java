package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import co.elastic.opamp.client.internal.state.InstanceUidState;
import com.google.protobuf.ByteString;
import opamp.proto.Opamp;

public final class InstanceUidVisitor implements AgentToServerVisitor {
  private final InstanceUidState instanceUidState;

  public static InstanceUidVisitor create(InstanceUidState instanceUidState) {
    return new InstanceUidVisitor(instanceUidState);
  }

  private InstanceUidVisitor(InstanceUidState instanceUidState) {
    this.instanceUidState = instanceUidState;
  }

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setInstanceUid(ByteString.copyFrom(instanceUidState.get()));
  }
}
