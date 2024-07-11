package co.elastic.opamp.client.internal.visitors;

import opamp.proto.Opamp;

public class SequenceNumberVisitor implements AgentToServerVisitor {
  private int sequenceNumber = 0;

  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {
    builder.setSequenceNum(++sequenceNumber);
  }
}
