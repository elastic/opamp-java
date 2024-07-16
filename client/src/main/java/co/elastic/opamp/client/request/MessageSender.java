package co.elastic.opamp.client.request;

import co.elastic.opamp.client.request.impl.OkHttpMessageSender;
import opamp.proto.Opamp;

public interface MessageSender {

  static MessageSender create(String url) {
    return OkHttpMessageSender.create(url);
  }

  void send(Opamp.AgentToServer message, RequestCallback callback);
}
