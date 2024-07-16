package co.elastic.opamp.client.request;

import co.elastic.opamp.client.request.impl.OkHttpService;
import opamp.proto.Opamp;

public interface Service {

  static Service create(String url) {
    return OkHttpService.create(url);
  }

  void sendMessage(Opamp.AgentToServer message, RequestCallback callback);
}
