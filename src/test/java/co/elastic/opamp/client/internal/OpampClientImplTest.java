package co.elastic.opamp.client.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.request.HttpService;
import co.elastic.opamp.client.state.AgentDescriptionState;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
    ArgumentCaptor<Opamp.AgentToServer> agentToServerCaptor =
        ArgumentCaptor.forClass(Opamp.AgentToServer.class);

    buildCustomClient(callback, createVisitorsWith(descriptionVisitor, mockVisitor)).sendMessage();

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

  private OpampClientVisitors createVisitorsWith(AgentToServerVisitor... visitors) {
    OpampClientVisitors clientVisitors = mock();
    doReturn(Arrays.asList(visitors)).when(clientVisitors).asList();
    return clientVisitors;
  }

  private AgentDescriptionState createAgentDescriptionWithServiceName(String serviceName) {
    Map<String, String> identifyingValues = new HashMap<>();
    identifyingValues.put("service.name", serviceName);
    return AgentDescriptionState.create(identifyingValues);
  }

  private OpampClientImpl buildCustomClient(
      OpampClient.Callback callback, OpampClientVisitors visitors) {
    return new OpampClientImpl(service, RequestContext.newBuilder(), callback, visitors);
  }
}
