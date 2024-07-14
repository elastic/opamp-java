package co.elastic.opamp.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.request.HttpService;
import co.elastic.opamp.client.state.AgentDescriptionState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OpampClientImplTest {
  private HttpService service;

  @BeforeEach
  void setUp() {
    service = mock();
  }

  @Test
  void verifySendMessage() throws IOException {
    OpampClient.Callback callback = mock();
    AgentToServerVisitor descriptionVisitor =
        AgentDescriptionVisitor.create(createAgentDescriptionWithServiceName("startTest"));
    AgentToServerVisitor mockVisitor = mock();
    List<AgentToServerVisitor> visitors = new ArrayList<>();
    visitors.add(descriptionVisitor);
    visitors.add(mockVisitor);
    ArgumentCaptor<Opamp.AgentToServer> agentToServerCaptor =
        ArgumentCaptor.forClass(Opamp.AgentToServer.class);

    buildCustomClient(callback, visitors).sendMessage();

    verify(service).sendMessage(agentToServerCaptor.capture());
    verify(mockVisitor).visit(notNull(), notNull());
    assertEquals(
        "startTest",
        agentToServerCaptor
            .getValue()
            .getAgentDescription()
            .getIdentifyingAttributes(0)
            .getValue()
            .getStringValue());
  }

  @Test
  void verifyAvailableVisitors() {
    OpampClientImpl client = (OpampClientImpl) OpampClient.builder().build(mock());

    assertThat(client.getVisitorsForTest())
        .extracting("class")
        .containsExactlyInAnyOrder(
            AgentDescriptionVisitor.class,
            EffectiveConfigVisitor.class,
            RemoteConfigStatusVisitor.class,
            SequenceNumberVisitor.class,
            CapabilitiesVisitor.class,
            FlagsVisitor.class,
            InstanceUidVisitor.class,
            AgentDisconnectVisitor.class);
  }

  private AgentDescriptionState createAgentDescriptionWithServiceName(String serviceName) {
    Map<String, String> identifyingValues = new HashMap<>();
    identifyingValues.put("service.name", serviceName);
    return AgentDescriptionState.create(identifyingValues);
  }

  private OpampClientImpl buildCustomClient(
      OpampClient.Callback callback, List<AgentToServerVisitor> visitors) {
    return new OpampClientImpl(service, RequestContext.newBuilder(), callback, visitors);
  }
}
