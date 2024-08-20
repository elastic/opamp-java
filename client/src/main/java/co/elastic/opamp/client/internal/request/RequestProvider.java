package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.request.Request;

public final class RequestProvider {
  private final OpampClientVisitors visitors;
  private RequestBuilder requestBuilder;

  public static RequestProvider create(OpampClientVisitors visitors) {
    RequestProvider requestProvider = new RequestProvider(visitors);
    requestProvider.resetBuilder();
    return requestProvider;
  }

  public void stop() {
    requestBuilder.stop();
  }

  public void disableCompression() {
    requestBuilder.disableCompression();
  }

  public Request getRequest() {
    Request request = requestBuilder.build();
    resetBuilder();
    return request;
  }

  private RequestProvider(OpampClientVisitors visitors) {
    this.visitors = visitors;
  }

  private void resetBuilder() {
    requestBuilder = RequestBuilder.create(visitors);
  }
}
