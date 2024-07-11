package co.elastic.opamp.client;

import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.configuration.Configuration;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.requests.OpampService;
import java.util.ArrayList;
import java.util.List;

public interface OpampClient {

  static OpampClient create(OpampService service, String serviceName, String serviceVersion) {
    List<AgentToServerVisitor> constantVisitors = new ArrayList<>();
    constantVisitors.add(new AgentDescriptionVisitor(serviceName, serviceVersion));
    constantVisitors.add(new CapabilitiesVisitor());
    constantVisitors.add(new EffectiveConfigVisitor(new Configuration()));
    constantVisitors.add(new FlagsVisitor());
    constantVisitors.add(new InstanceUidVisitor());
    constantVisitors.add(new RemoteConfigStatusVisitor());
    constantVisitors.add(new SequenceNumberVisitor());
    constantVisitors.add(new AgentDisconnectVisitor());
    return new OpampClientImpl(service, constantVisitors);
  }

  void start();

  void stop();
}
