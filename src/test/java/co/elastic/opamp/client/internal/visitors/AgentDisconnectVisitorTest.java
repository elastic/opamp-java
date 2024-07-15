package co.elastic.opamp.client.internal.visitors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.internal.RequestContext;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentDisconnectVisitorTest {
  private AgentDisconnectVisitor agentDisconnectVisitor;

  @BeforeEach
  void setUp() {
    agentDisconnectVisitor = new AgentDisconnectVisitor();
  }

  @Test
  void whenStopEnabled_sendDisconnectMessage() {
    Opamp.AgentToServer.Builder builder = mock();

    agentDisconnectVisitor.visit(RequestContext.newBuilder().stop().buildAndReset(), builder);

    verify(builder).setAgentDisconnect((Opamp.AgentDisconnect) notNull());
  }

  @Test
  void whenStopNotEnabled_doNotSendDisconnectMessage() {
    Opamp.AgentToServer.Builder builder = mock();

    agentDisconnectVisitor.visit(RequestContext.newBuilder().buildAndReset(), builder);

    verify(builder, never()).setAgentDisconnect((Opamp.AgentDisconnect) any());
  }
}
