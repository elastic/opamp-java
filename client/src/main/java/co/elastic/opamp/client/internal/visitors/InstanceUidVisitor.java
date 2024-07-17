package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.handlers.InstanceUidHandler;
import co.elastic.opamp.client.internal.request.RequestContext;
import com.google.protobuf.ByteString;
import opamp.proto.Opamp;

public class InstanceUidVisitor implements AgentToServerVisitor {
  private final InstanceUidHandler instanceUidHandler;

  public static InstanceUidVisitor create(InstanceUidHandler instanceUidHandler) {
    return new InstanceUidVisitor(instanceUidHandler);
  }

  private InstanceUidVisitor(InstanceUidHandler instanceUidHandler) {
    this.instanceUidHandler = instanceUidHandler;
  }

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setInstanceUid(ByteString.copyFrom(getUuid()));
  }

  private byte[] getUuid() {
    return instanceUidHandler.get();
  }
}
