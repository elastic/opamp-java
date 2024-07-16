package co.elastic.opamp.client.internal.state;

import opamp.proto.Opamp;

public class EffectiveConfigState extends StateHolder<Opamp.EffectiveConfig> {

  static EffectiveConfigState create() {
    return new EffectiveConfigState(Opamp.EffectiveConfig.newBuilder().build());
  }

  private EffectiveConfigState(Opamp.EffectiveConfig initialState) {
    super(initialState);
  }
}
