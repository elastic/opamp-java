package co.elastic.opamp.client.internal.dispatcher;

import co.elastic.opamp.client.internal.dispatcher.impl.MessageDispatcherImpl;
import co.elastic.opamp.client.request.MessageSender;
import java.util.concurrent.TimeUnit;

public interface MessageDispatcher {
  static MessageDispatcher create(MessageSender sender) {
    return MessageDispatcherImpl.create(sender);
  }

  void dispatchWithDelay(long delay, TimeUnit unit);

  void dispatchNow();

  void setMessageBuilder(MessageBuilder messageBuilder);

  void setResponseHandler(ResponseHandler responseHandler);
}
