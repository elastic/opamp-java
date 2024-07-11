package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.requests.OpampService;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public class OpampClientImpl implements OpampClient {
  private final OpampService service;
  private final Supplier<ClientContext.Builder> contextBuilderSupplier;
  private final List<AgentToServerVisitor> visitors;
  private ClientContext.Builder contextBuilder;

  OpampClientImpl(
      OpampService service,
      Supplier<ClientContext.Builder> contextBuilderSupplier,
      List<AgentToServerVisitor> visitors) {
    this.service = service;
    this.contextBuilderSupplier = contextBuilderSupplier;
    this.visitors = visitors;
  }

  @Override
  public void start() {
    sendMessage();
  }

  @Override
  public void stop() {
    getContextBuilder().stop();
    sendMessage();
  }

  private Opamp.AgentToServer buildMessage() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    visitors.forEach(visitor -> visitor.visit(buildContext(), builder));
    return builder.build();
  }

  private void sendMessage() {
    try {
      Opamp.ServerToAgent serverToAgent = service.sendMessage(buildMessage());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ClientContext buildContext() {
    ClientContext context = getContextBuilder().build();
    contextBuilder = null;
    return context;
  }

  private ClientContext.Builder getContextBuilder() {
    if (contextBuilder == null) {
      contextBuilder = contextBuilderSupplier.get();
    }

    return contextBuilder;
  }
}
