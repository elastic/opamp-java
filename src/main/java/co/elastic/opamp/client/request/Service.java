package co.elastic.opamp.client.request;

import okhttp3.OkHttpClient;
import opamp.proto.Opamp;

public interface Service {

  static Service create(String url) {
    OkHttpClient client = new OkHttpClient();
    return new OkHttpService(client, url);
  }

  void sendMessage(Opamp.AgentToServer message, RequestCallback callback);
}
