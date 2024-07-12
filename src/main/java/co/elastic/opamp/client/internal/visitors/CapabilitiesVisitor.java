package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.RequestContext;
import opamp.proto.Opamp;

public class CapabilitiesVisitor implements AgentToServerVisitor {

  @Override
  public void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setCapabilities(
        Opamp.AgentCapabilities.AgentCapabilities_ReportsStatus_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_AcceptsRemoteConfig_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_ReportsEffectiveConfig_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_ReportsRemoteConfig_VALUE);
  }
}
