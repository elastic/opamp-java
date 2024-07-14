package co.elastic.opamp.client;

import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.requests.OpampService;
import co.elastic.opamp.client.state.AgentDescriptionState;
import co.elastic.opamp.client.state.EffectiveConfigState;
import java.util.ArrayList;
import java.util.List;

public interface OpampClient {

  static OpampClient create(
      OpampService service,
      AgentDescriptionState agentDescriptionState,
      EffectiveConfigState effectiveConfigState) {
    List<AgentToServerVisitor> constantVisitors = new ArrayList<>();
    constantVisitors.add(AgentDescriptionVisitor.create(agentDescriptionState));
    constantVisitors.add(EffectiveConfigVisitor.create(effectiveConfigState));
    constantVisitors.add(new CapabilitiesVisitor());
    constantVisitors.add(new FlagsVisitor());
    constantVisitors.add(new InstanceUidVisitor());
    constantVisitors.add(new RemoteConfigStatusVisitor());
    constantVisitors.add(new SequenceNumberVisitor());
    constantVisitors.add(new AgentDisconnectVisitor());
    //    return new OpampClientImpl(service, constantVisitors);
    throw new UnsupportedOperationException();
  }

  void start();

  void stop();
}
