package co.elastic.opamp.client.internal;

import static org.mockito.Mockito.mock;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.request.HttpService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;

class OpampClientImplTest {
  private HttpService service;
  private RequestContext.Builder contextBuilder;

  @BeforeEach
  void setUp() {
    service = mock();
    contextBuilder = mock();
  }

  private OpampClient buildClient(
      OpampClient.Callback callback, List<AgentToServerVisitor> visitors) {
    return new OpampClientImpl(service, contextBuilder, callback, visitors);
  }
}
