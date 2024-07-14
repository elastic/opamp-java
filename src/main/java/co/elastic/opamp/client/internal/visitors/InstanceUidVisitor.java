package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.RequestContext;
import com.github.f4b6a3.uuid.UuidCreator;
import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.util.UUID;
import opamp.proto.Opamp;

public class InstanceUidVisitor implements AgentToServerVisitor {
  private byte[] uuid;

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setInstanceUid(ByteString.copyFrom(getUuid()));
  }

  private byte[] getUuid() {
    if (uuid == null) {
      UUID random = UuidCreator.getTimeOrderedEpoch();
      ByteBuffer buffer = ByteBuffer.allocate(16);
      buffer.putLong(random.getMostSignificantBits());
      buffer.putLong(random.getLeastSignificantBits());
      uuid = buffer.array();
    }

    return uuid;
  }
}