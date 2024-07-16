package co.elastic.opamp.client.internal.state;

public final class OpampClientState {
  public final RemoteConfigStatusState remoteConfigStatusState;
  public final SequenceNumberState sequenceNumberState;
  public final AgentDescriptionState agentDescriptionState;
  public final EffectiveConfigState effectiveConfigState;

  public static OpampClientState create() {
    return new OpampClientState(
        RemoteConfigStatusState.create(),
        SequenceNumberState.create(),
        AgentDescriptionState.create(),
        EffectiveConfigState.create());
  }

  private OpampClientState(
      RemoteConfigStatusState remoteConfigStatusState,
      SequenceNumberState sequenceNumberState,
      AgentDescriptionState agentDescriptionState,
      EffectiveConfigState effectiveConfigState) {
    this.remoteConfigStatusState = remoteConfigStatusState;
    this.sequenceNumberState = sequenceNumberState;
    this.agentDescriptionState = agentDescriptionState;
    this.effectiveConfigState = effectiveConfigState;
  }
}
