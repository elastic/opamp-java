package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.request.Request;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public final class RequestBuilder {
  private final OpampClientVisitors visitors;
  private final Supplier<RequestContext.Builder> contextBuilderSupplier;
  private RequestContext.Builder contextBuilder;

  public static RequestBuilder create(OpampClientVisitors visitors) {
    return new RequestBuilder(visitors, RequestContext::newBuilder);
  }

  RequestBuilder(
      OpampClientVisitors visitors, Supplier<RequestContext.Builder> contextBuilderSupplier) {
    this.visitors = visitors;
    this.contextBuilderSupplier = contextBuilderSupplier;
    contextBuilder = contextBuilderSupplier.get();
  }

  public Request buildAndReset() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    RequestContext requestContext = contextBuilder.build();
    visitors.asList().forEach(visitor -> visitor.visit(requestContext, builder));
    contextBuilder = contextBuilderSupplier.get();
    return Request.create(builder.build());
  }

  public void stop() {
    contextBuilder.stop();
  }

  public void disableCompression() {
    contextBuilder.disableCompression();
  }
}
