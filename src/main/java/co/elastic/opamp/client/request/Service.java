package co.elastic.opamp.client.request;

import java.io.IOException;
import okhttp3.OkHttpClient;
import opamp.proto.Opamp;

public interface Service {

  static Service create(String url) {
    OkHttpClient client = new OkHttpClient();
    return new OkHttpService(client, url);
  }

  Opamp.ServerToAgent sendMessage(Opamp.AgentToServer message) throws IOException;
}
