package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.ClientContext;
import com.google.protobuf.ByteString;
import co.elastic.opamp.client.configuration.Configuration;
import opamp.proto.Opamp;

public class EffectiveConfigVisitor implements AgentToServerVisitor {
  private final Configuration configuration;

  public EffectiveConfigVisitor(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void visit(ClientContext clientContext, Opamp.AgentToServer.Builder builder) {
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
