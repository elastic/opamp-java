package co.elastic.opamp.sample;

import co.elastic.opamp.client.state.State;
import opamp.proto.Opamp;

public class MyEffectiveConfigState extends State<Opamp.EffectiveConfig> {

  public void updateConfig(Opamp.AgentRemoteConfig remoteConfig) {
    // Update the effective config

    notifyObservers(); // This will ensure that the client will query this state to send the
    // effective config in the next request.
  }

  @Override
  public Opamp.EffectiveConfig get() {
    // return some value from a local db or file.
    return Opamp.EffectiveConfig.newBuilder().build();
  }
}
