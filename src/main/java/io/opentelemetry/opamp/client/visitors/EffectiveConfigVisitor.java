package io.opentelemetry.opamp.client.visitors;

import com.google.protobuf.ByteString;
import io.opentelemetry.opamp.client.configuration.Configuration;
import opamp.proto.Opamp;

public class EffectiveConfigVisitor implements AgentToServerVisitor {
  private final Configuration configuration;

  public EffectiveConfigVisitor(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void visit(Opamp.AgentToServer.Builder builder) {
    Opamp.AgentConfigFile configFile = getAgentConfigFile();
    Opamp.AgentConfigMap configMap =
        Opamp.AgentConfigMap.newBuilder().putConfigMap("default", configFile).build();
    Opamp.EffectiveConfig effectiveConfig =
        Opamp.EffectiveConfig.newBuilder().setConfigMap(configMap).build();

    builder.setEffectiveConfig(effectiveConfig);
  }

  private Opamp.AgentConfigFile getAgentConfigFile() {
    return Opamp.AgentConfigFile.newBuilder()
        .setBody(ByteString.copyFromUtf8(configuration.toJson()))
        .setContentType("application/json")
        .build();
  }
}
