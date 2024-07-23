package co.elastic.opamp.client.internal.state;

import opamp.proto.Opamp;

public final class AgentDescriptionState extends StateHolder<Opamp.AgentDescription> {

  static AgentDescriptionState create() {
    return new AgentDescriptionState(Opamp.AgentDescription.newBuilder().build());
  }

  private AgentDescriptionState(Opamp.AgentDescription initialState) {
    super(initialState);
  }
}
