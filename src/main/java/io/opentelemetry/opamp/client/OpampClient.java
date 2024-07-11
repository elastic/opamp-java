package io.opentelemetry.opamp.client;

import io.opentelemetry.opamp.client.configuration.Configuration;
import io.opentelemetry.opamp.client.internal.visitors.AgentDescriptionVisitor;
import io.opentelemetry.opamp.client.internal.visitors.AgentDisconnectVisitor;
import io.opentelemetry.opamp.client.internal.visitors.AgentToServerVisitor;
import io.opentelemetry.opamp.client.internal.visitors.CapabilitiesVisitor;
import io.opentelemetry.opamp.client.internal.visitors.EffectiveConfigVisitor;
import io.opentelemetry.opamp.client.internal.visitors.FlagsVisitor;
import io.opentelemetry.opamp.client.internal.visitors.InstanceUidVisitor;
import io.opentelemetry.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import io.opentelemetry.opamp.client.internal.visitors.SequenceNumberVisitor;
import io.opentelemetry.opamp.client.request.Operation;
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
    constantVisitors.add(new AgentDisconnectVisitor());
    return new OpampClientImpl(operation, constantVisitors);
  }

  void start();

  void stop();
}
