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
      FlagsVisitor flagsVisitor,
      InstanceUidVisitor instanceUidVisitor,
      AgentDisconnectVisitor agentDisconnectVisitor) {
    List<AgentToServerVisitor> visitors = new ArrayList<>();
    visitors.add(agentDescriptionVisitor);
    visitors.add(effectiveConfigVisitor);
    visitors.add(remoteConfigStatusVisitor);
    visitors.add(sequenceNumberVisitor);
    visitors.add(capabilitiesVisitor);
    visitors.add(flagsVisitor);
    visitors.add(instanceUidVisitor);
    visitors.add(agentDisconnectVisitor);
    allVisitors = Collections.unmodifiableList(visitors);
  }

  public List<AgentToServerVisitor> asList() {
    return allVisitors;
  }
}
