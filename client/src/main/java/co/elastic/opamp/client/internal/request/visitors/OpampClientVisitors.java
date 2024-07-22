package co.elastic.opamp.client.internal.request.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OpampClientVisitors {
  private final List<AgentToServerVisitor> allVisitors;

  public OpampClientVisitors(
      AgentDescriptionVisitor agentDescriptionVisitor,
      EffectiveConfigVisitor effectiveConfigVisitor,
      RemoteConfigStatusVisitor remoteConfigStatusVisitor,
      SequenceNumberVisitor sequenceNumberVisitor,
      CapabilitiesVisitor capabilitiesVisitor,
      InstanceUidVisitor instanceUidVisitor,
      FlagsVisitor flagsVisitor,
      AgentDisconnectVisitor agentDisconnectVisitor) {
    List<AgentToServerVisitor> visitors = new ArrayList<>();
    visitors.add(agentDescriptionVisitor);
    visitors.add(effectiveConfigVisitor);
    visitors.add(remoteConfigStatusVisitor);
    visitors.add(sequenceNumberVisitor);
    visitors.add(capabilitiesVisitor);
    visitors.add(instanceUidVisitor);
    visitors.add(flagsVisitor);
    visitors.add(agentDisconnectVisitor);
    allVisitors = Collections.unmodifiableList(visitors);
  }

  public List<AgentToServerVisitor> asList() {
    return allVisitors;
  }
}
