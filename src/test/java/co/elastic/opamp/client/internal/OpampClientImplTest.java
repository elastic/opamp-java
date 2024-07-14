package co.elastic.opamp.client.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
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
  private RequestContext.Builder contextBuilder;

  @BeforeEach
  void setUp() {
    service = mock();
    contextBuilder = mock();
  }

  @Test
  void verifySendMessage() throws IOException {
    OpampClient.Callback callback = mock();
    RequestContext requestContext = RequestContext.newBuilder().buildAndReset();
    doReturn(requestContext).when(contextBuilder).buildAndReset();
    AgentToServerVisitor descriptionVisitor =
        AgentDescriptionVisitor.create(createAgentDescriptionWithServiceName("startTest"));
    AgentToServerVisitor mockVisitor = mock();
    List<AgentToServerVisitor> visitors = new ArrayList<>();
    visitors.add(descriptionVisitor);
    visitors.add(mockVisitor);
    ArgumentCaptor<Opamp.AgentToServer> agentToServerCaptor =
        ArgumentCaptor.forClass(Opamp.AgentToServer.class);

    buildClient(callback, visitors).sendMessage();

    verify(service).sendMessage(agentToServerCaptor.capture());
    verify(mockVisitor).visit(eq(requestContext), any());
    assertEquals(
        "startTest",
        agentToServerCaptor
            .getValue()
            .getAgentDescription()
            .getIdentifyingAttributes(0)
            .getValue()
            .getStringValue());
  }

  private AgentDescriptionState createAgentDescriptionWithServiceName(String serviceName) {
    Map<String, String> identifyingValues = new HashMap<>();
    identifyingValues.put("service.name", serviceName);
    return AgentDescriptionState.create(identifyingValues);
  }

  private OpampClientImpl buildClient(
      OpampClient.Callback callback, List<AgentToServerVisitor> visitors) {
    return new OpampClientImpl(service, contextBuilder, callback, visitors);
  }
}
