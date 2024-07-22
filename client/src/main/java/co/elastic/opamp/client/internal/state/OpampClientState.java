package co.elastic.opamp.client.internal.state;

public final class OpampClientState {
  public final RemoteConfigStatusState remoteConfigStatusState;
  public final SequenceNumberState sequenceNumberState;
  public final AgentDescriptionState agentDescriptionState;
  public final EffectiveConfigState effectiveConfigState;
  public final CapabilitiesState capabilitiesState;
  public final InstanceUidState instanceUidState;

  public static OpampClientState create() {
    return new OpampClientState(
        RemoteConfigStatusState.create(),
        SequenceNumberState.create(),
        AgentDescriptionState.create(),
        EffectiveConfigState.create(),
        CapabilitiesState.create(),
        InstanceUidState.createRandom());
  }

  public OpampClientState(
      RemoteConfigStatusState remoteConfigStatusState,
      SequenceNumberState sequenceNumberState,
      AgentDescriptionState agentDescriptionState,
      EffectiveConfigState effectiveConfigState,
      CapabilitiesState capabilitiesState,
      InstanceUidState instanceUidState) {
    this.remoteConfigStatusState = remoteConfigStatusState;
    this.sequenceNumberState = sequenceNumberState;
    this.agentDescriptionState = agentDescriptionState;
    this.effectiveConfigState = effectiveConfigState;
    this.capabilitiesState = capabilitiesState;
    this.instanceUidState = instanceUidState;
  }
}
