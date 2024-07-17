package co.elastic.opamp.client.internal.dispatcher;

import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestSender;
import java.util.function.Supplier;

public class RequestDispatcher implements Runnable {
  private final RequestSender sender;
  private RequestSender.Callback callback;
  private Supplier<Request> requestSupplier;

  public RequestDispatcher(RequestSender sender) {
    this.sender = sender;
  }

  @Override
  public void run() {
    Request request = (requestSupplier != null) ? requestSupplier.get() : null;
    if (request == null) {
      return;
    }

    sender.send(request.getAgentToServer(), callback);
  }

  public void setRequestSupplier(Supplier<Request> requestSupplier) {
    this.requestSupplier = requestSupplier;
  }

  public void setRequestCallback(RequestSender.Callback callback) {
    this.callback = callback;
  }
}
