package io.opentelemetry.opamp.client;

import io.opentelemetry.opamp.client.configuration.Configuration;
import io.opentelemetry.opamp.client.request.Operation;
import io.opentelemetry.opamp.client.visitors.AgentDescriptionVisitor;
import io.opentelemetry.opamp.client.visitors.AgentDisconnectVisitor;
import io.opentelemetry.opamp.client.visitors.AgentToServerVisitor;
import io.opentelemetry.opamp.client.visitors.CapabilitiesVisitor;
import io.opentelemetry.opamp.client.visitors.EffectiveConfigVisitor;
import io.opentelemetry.opamp.client.visitors.FlagsVisitor;
import io.opentelemetry.opamp.client.visitors.InstanceUidVisitor;
import io.opentelemetry.opamp.client.visitors.RemoteConfigStatusVisitor;
import io.opentelemetry.opamp.client.visitors.SequenceNumberVisitor;
import java.util.ArrayList;
import java.util.List;

public interface OpampClient {

  static OpampClient create(Operation operation, String serviceName, String serviceVersion) {
    List<AgentToServerVisitor> constantVisitors = new ArrayList<>();
    constantVisitors.add(new AgentDescriptionVisitor(serviceName, serviceVersion));
    constantVisitors.add(new CapabilitiesVisitor());
    constantVisitors.add(new EffectiveConfigVisitor(new Configuration()));
    constantVisitors.add(new FlagsVisitor());
    constantVisitors.add(new InstanceUidVisitor());
    constantVisitors.add(new RemoteConfigStatusVisitor());
    constantVisitors.add(new SequenceNumberVisitor());
    return new OpampClientImpl(operation, constantVisitors, new AgentDisconnectVisitor());
  }

  void start();

  void stop();
}
