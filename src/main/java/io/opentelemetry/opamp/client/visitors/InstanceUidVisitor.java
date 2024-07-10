package io.opentelemetry.opamp.client.visitors;

import com.google.protobuf.ByteString;
import java.util.UUID;
import opamp.proto.Opamp;

public class InstanceUidVisitor implements AgentToServerVisitor {

  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {
    builder.setInstanceUid(ByteString.copyFromUtf8(UUID.randomUUID().toString()));
  }
}
