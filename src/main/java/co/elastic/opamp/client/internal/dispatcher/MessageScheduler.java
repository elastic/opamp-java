package co.elastic.opamp.client.internal.dispatcher;

import co.elastic.opamp.client.request.Service;
import java.util.concurrent.TimeUnit;

public interface MessageScheduler {
  static MessageScheduler create(Service service) {
    return MessageSchedulerImpl.create(service);
  }

  void scheduleWithDelay(long delay, TimeUnit unit);

  void scheduleNow();

  void setMessageBuilder(MessageBuilder messageBuilder);

  void setResponseHandler(ResponseHandler responseHandler);
}
