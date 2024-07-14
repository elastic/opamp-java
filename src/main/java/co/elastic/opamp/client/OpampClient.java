package co.elastic.opamp.client;

import co.elastic.opamp.client.internal.state.SequenceNumberState;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.requests.HttpService;
import co.elastic.opamp.client.state.AgentDescriptionState;
import co.elastic.opamp.client.state.EffectiveConfigState;
import co.elastic.opamp.client.state.RemoteConfigStatusState;
import java.util.ArrayList;
import java.util.List;

public interface OpampClient {

  static OpampClient create(
      HttpService service,
      AgentDescriptionState agentDescriptionState,
      EffectiveConfigState effectiveConfigState,
      RemoteConfigStatusState remoteConfigStatusState) {
    SequenceNumberState sequenceNumberState = SequenceNumberState.create();
    List<AgentToServerVisitor> visitors = new ArrayList<>();
    visitors.add(AgentDescriptionVisitor.create(agentDescriptionState));
    visitors.add(EffectiveConfigVisitor.create(effectiveConfigState));
    visitors.add(RemoteConfigStatusVisitor.create(remoteConfigStatusState));
    visitors.add(SequenceNumberVisitor.create(sequenceNumberState));
    visitors.add(new CapabilitiesVisitor());
    visitors.add(new FlagsVisitor());
    visitors.add(new InstanceUidVisitor());
    visitors.add(new AgentDisconnectVisitor());
    //    return new OpampClientImpl(service, visitors);
    throw new UnsupportedOperationException();
  }

  void start();

  void stop();
}
