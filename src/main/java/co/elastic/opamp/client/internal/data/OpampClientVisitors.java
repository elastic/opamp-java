package co.elastic.opamp.client.internal.data;

import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import java.util.List;

public final class OpampClientVisitors {
  public final AgentDescriptionVisitor agentDescriptionVisitor;
  public final EffectiveConfigVisitor effectiveConfigVisitor;
  public final RemoteConfigStatusVisitor remoteConfigStatusVisitor;
  public final SequenceNumberVisitor sequenceNumberVisitor;
  public final CapabilitiesVisitor capabilitiesVisitor;
  public final FlagsVisitor flagsVisitor;
  public final InstanceUidVisitor instanceUidVisitor;
  public final AgentDisconnectVisitor agentDisconnectVisitor;

  private List<AgentToServerVisitor> allVisitors;

  public OpampClientVisitors(
      AgentDescriptionVisitor agentDescriptionVisitor,
      EffectiveConfigVisitor effectiveConfigVisitor,
      RemoteConfigStatusVisitor remoteConfigStatusVisitor,
      SequenceNumberVisitor sequenceNumberVisitor,
      CapabilitiesVisitor capabilitiesVisitor,
      FlagsVisitor flagsVisitor,
      InstanceUidVisitor instanceUidVisitor,
      AgentDisconnectVisitor agentDisconnectVisitor) {
    this.agentDescriptionVisitor = agentDescriptionVisitor;
    this.effectiveConfigVisitor = effectiveConfigVisitor;
    this.remoteConfigStatusVisitor = remoteConfigStatusVisitor;
    this.sequenceNumberVisitor = sequenceNumberVisitor;
    this.capabilitiesVisitor = capabilitiesVisitor;
    this.flagsVisitor = flagsVisitor;
    this.instanceUidVisitor = instanceUidVisitor;
    this.agentDisconnectVisitor = agentDisconnectVisitor;
  }

  public List<AgentToServerVisitor> asList() {
    if (allVisitors == null) {
      allVisitors =
          List.of(
              agentDescriptionVisitor,
              effectiveConfigVisitor,
              remoteConfigStatusVisitor,
              sequenceNumberVisitor,
              capabilitiesVisitor,
              flagsVisitor,
              instanceUidVisitor,
              agentDisconnectVisitor);
    }

    return allVisitors;
  }
}
