package co.elastic.opamp.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.scheduler.Message;
import co.elastic.opamp.client.internal.scheduler.MessageScheduler;
import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.state.AgentDescriptionState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OpampClientImplTest {
  private MessageScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = mock();
  }

  @Test
  void verifyStart() {
    buildClient(null).start();

    verify(scheduler).scheduleNow();
  }

  @Test
  void verifyMessageBuilding() {
    OpampClient.Callback callback = mock();
    AgentToServerVisitor descriptionVisitor =
        AgentDescriptionVisitor.create(createAgentDescriptionWithServiceName("startTest"));
    AgentToServerVisitor mockVisitor = mock();
    ArgumentCaptor<RequestContext> contextCaptor = ArgumentCaptor.forClass(RequestContext.class);

    Message message =
        buildClient(callback, createVisitorsWith(descriptionVisitor, mockVisitor)).buildMessage();

    verify(mockVisitor).visit(contextCaptor.capture(), notNull());
    RequestContext requestContext = contextCaptor.getValue();
    assertThat(requestContext.stop).isFalse();
    assertThat(requestContext.disableCompression).isFalse();
    assertEquals(
        "startTest",
        message
            .agentToServer
            .getAgentDescription()
            .getIdentifyingAttributes(0)
            .getValue()
            .getStringValue());
  }

  @Test
  void verifyMessageBuildingAfterStopIsCalled() {
    AgentToServerVisitor visitor = mock();
    OpampClientImpl client = buildClient(null, createVisitorsWith(visitor));
    ArgumentCaptor<RequestContext> captor = ArgumentCaptor.forClass(RequestContext.class);
    client.stop();

    client.buildMessage();

    verify(visitor).visit(captor.capture(), notNull());
    RequestContext context = captor.getValue();
    assertThat(context.stop).isTrue();
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

  private OpampClientImpl buildClient(OpampClient.Callback callback) {
    return buildClient(callback, mock());
  }

  private OpampClientImpl buildClient(OpampClient.Callback callback, OpampClientVisitors visitors) {
    return OpampClientImpl.create(scheduler, RequestContext.newBuilder(), visitors, callback);
  }
}
