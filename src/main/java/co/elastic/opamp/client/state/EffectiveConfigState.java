package co.elastic.opamp.client.state;

import co.elastic.opamp.client.internal.state.StateHolder;
import opamp.proto.Opamp;

public class EffectiveConfigState extends StateHolder<Opamp.EffectiveConfig> {

  public static EffectiveConfigState create(Opamp.EffectiveConfig config) {
    return new EffectiveConfigState(config);
  }

  private EffectiveConfigState(Opamp.EffectiveConfig initialState) {
    super(initialState);
  }
}
