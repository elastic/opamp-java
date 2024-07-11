package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.ClientContext;
import opamp.proto.Opamp;

public class SequenceNumberVisitor implements AgentToServerVisitor {
  private int sequenceNumber = 0;

  @Override
  public void visit(ClientContext clientContext, Opamp.AgentToServer.Builder builder) {
    builder.setSequenceNum(++sequenceNumber);
  }
}
