package co.elastic.opamp.client.internal.visitors;

import co.elastic.opamp.client.internal.request.RequestContext;
import co.elastic.opamp.client.internal.state.EffectiveConfigState;
import opamp.proto.Opamp;

public class EffectiveConfigVisitor extends CompressableAgentToServerVisitor {
  private final EffectiveConfigState effectiveConfig;

  public static EffectiveConfigVisitor create(EffectiveConfigState effectiveConfig) {
    EffectiveConfigVisitor visitor = new EffectiveConfigVisitor(effectiveConfig);
    effectiveConfig.addObserver(visitor);
    return visitor;
  }

  private EffectiveConfigVisitor(EffectiveConfigState effectiveConfig) {
    this.effectiveConfig = effectiveConfig;
  }

  @Override
  protected void doVisit(RequestContext requestContext, Opamp.AgentToServer.Builder builder) {
    builder.setEffectiveConfig(effectiveConfig.get());
  }
}
