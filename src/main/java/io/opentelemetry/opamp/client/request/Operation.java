package io.opentelemetry.opamp.client.request;

import java.io.IOException;
import okhttp3.OkHttpClient;
import opamp.proto.Opamp;

public interface Operation {

  static Operation create(String url) {
    OkHttpClient client = new OkHttpClient();
    return new OperationImpl(client, url);
  }

  Opamp.ServerToAgent sendMessage(Opamp.AgentToServer message) throws IOException;
}
