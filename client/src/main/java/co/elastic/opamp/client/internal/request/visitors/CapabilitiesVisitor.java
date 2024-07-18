package co.elastic.opamp.client.internal.request.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import co.elastic.opamp.client.internal.state.CapabilitiesState;
import opamp.proto.Opamp;

public class CapabilitiesVisitor implements AgentToServerVisitor {
  private final CapabilitiesState capabilitiesState;

  public static CapabilitiesVisitor create(CapabilitiesState capabilitiesState) {
    return new CapabilitiesVisitor(capabilitiesState);
  }

  private CapabilitiesVisitor(CapabilitiesState capabilitiesState) {
    this.capabilitiesState = capabilitiesState;
  }

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setCapabilities(capabilitiesState.get());
  }
}
