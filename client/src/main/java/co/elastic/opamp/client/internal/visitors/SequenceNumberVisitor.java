package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import co.elastic.opamp.client.internal.state.SequenceNumberState;
import opamp.proto.Opamp;

public class SequenceNumberVisitor implements AgentToServerVisitor {
  private final SequenceNumberState sequenceNumberState;

  public static SequenceNumberVisitor create(SequenceNumberState sequenceNumberState) {
    return new SequenceNumberVisitor(sequenceNumberState);
  }

  private SequenceNumberVisitor(SequenceNumberState sequenceNumberState) {
    this.sequenceNumberState = sequenceNumberState;
  }

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setSequenceNum(sequenceNumberState.get());
  }
}
