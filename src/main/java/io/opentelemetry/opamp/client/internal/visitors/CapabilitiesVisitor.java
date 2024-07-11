package io.opentelemetry.opamp.client.internal.visitors;

import opamp.proto.Opamp;

public class CapabilitiesVisitor implements AgentToServerVisitor {

  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {
    builder.setCapabilities(
        Opamp.AgentCapabilities.AgentCapabilities_ReportsStatus_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_AcceptsRemoteConfig_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_ReportsEffectiveConfig_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_ReportsRemoteConfig_VALUE);
  }
}
