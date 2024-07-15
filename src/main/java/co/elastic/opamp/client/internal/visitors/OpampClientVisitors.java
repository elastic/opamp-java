package co.elastic.opamp.client.internal.visitors;

import java.util.ArrayList;
import java.util.List;

public final class OpampClientVisitors {
  private final List<AgentToServerVisitor> allVisitors = new ArrayList<>();

  public OpampClientVisitors(
      AgentDescriptionVisitor agentDescriptionVisitor,
      EffectiveConfigVisitor effectiveConfigVisitor,
      RemoteConfigStatusVisitor remoteConfigStatusVisitor,
      SequenceNumberVisitor sequenceNumberVisitor,
      CapabilitiesVisitor capabilitiesVisitor,
      FlagsVisitor flagsVisitor,
      InstanceUidVisitor instanceUidVisitor,
      AgentDisconnectVisitor agentDisconnectVisitor) {
    allVisitors.add(agentDescriptionVisitor);
    allVisitors.add(effectiveConfigVisitor);
    allVisitors.add(remoteConfigStatusVisitor);
    allVisitors.add(sequenceNumberVisitor);
    allVisitors.add(capabilitiesVisitor);
    allVisitors.add(flagsVisitor);
    allVisitors.add(instanceUidVisitor);
    allVisitors.add(agentDisconnectVisitor);
  }

  public List<AgentToServerVisitor> asList() {
    return allVisitors;
  }
}
