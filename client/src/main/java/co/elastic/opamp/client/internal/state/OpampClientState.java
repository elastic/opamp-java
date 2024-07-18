package co.elastic.opamp.client.internal.state;

public final class OpampClientState {
  public final RemoteConfigStatusState remoteConfigStatusState;
  public final SequenceNumberState sequenceNumberState;
  public final AgentDescriptionState agentDescriptionState;
  public final EffectiveConfigState effectiveConfigState;
  public final CapabilitiesState capabilitiesState;

  public static OpampClientState create() {
    return new OpampClientState(
        RemoteConfigStatusState.create(),
        SequenceNumberState.create(),
        AgentDescriptionState.create(),
        EffectiveConfigState.create(),
        CapabilitiesState.create());
  }

  public OpampClientState(
      RemoteConfigStatusState remoteConfigStatusState,
      SequenceNumberState sequenceNumberState,
      AgentDescriptionState agentDescriptionState,
      EffectiveConfigState effectiveConfigState,
      CapabilitiesState capabilitiesState) {
    this.remoteConfigStatusState = remoteConfigStatusState;
    this.sequenceNumberState = sequenceNumberState;
    this.agentDescriptionState = agentDescriptionState;
    this.effectiveConfigState = effectiveConfigState;
    this.capabilitiesState = capabilitiesState;
  }
}
