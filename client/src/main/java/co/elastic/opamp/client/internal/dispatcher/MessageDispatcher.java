package co.elastic.opamp.client.internal.dispatcher;

import co.elastic.opamp.client.request.Service;
import java.util.concurrent.TimeUnit;

public interface MessageDispatcher {
  static MessageDispatcher create(Service service) {
    return MessageDispatcherImpl.create(service);
  }

  void dispatchWithDelay(long delay, TimeUnit unit);

  void dispatchNow();

  void setMessageBuilder(MessageBuilder messageBuilder);

  void setResponseHandler(ResponseHandler responseHandler);
}
