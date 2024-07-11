package io.opentelemetry.opamp.client.requests;

import java.io.IOException;
import okhttp3.OkHttpClient;
import opamp.proto.Opamp;

public interface OpampService {

  static OpampService create(String url) {
    OkHttpClient client = new OkHttpClient();
    return new OpampServiceImpl(client, url);
  }

  Opamp.ServerToAgent sendMessage(Opamp.AgentToServer message) throws IOException;
}
