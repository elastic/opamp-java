package co.elastic.opamp.client.requests;

import java.io.IOException;
import okhttp3.OkHttpClient;
import opamp.proto.Opamp;

public interface HttpService {

  static HttpService create(String url) {
    OkHttpClient client = new OkHttpClient();
    return new OkHttpHttpService(client, url);
  }

  Opamp.ServerToAgent sendMessage(Opamp.AgentToServer message) throws IOException;
}
