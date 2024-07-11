package io.opentelemetry.opamp.client.request;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import opamp.proto.Opamp;

public class OpampServiceImpl implements OpampService {
  private final OkHttpClient client;
  private final String url;

  OpampServiceImpl(OkHttpClient client, String url) {
    this.client = client;
    this.url = url;
  }

  @Override
  public Opamp.ServerToAgent sendMessage(Opamp.AgentToServer message) throws IOException {
    Request.Builder builder = new Request.Builder().url(url);
    String contentType = "application/x-protobuf";
    builder.addHeader("Content-Type", contentType);

    RequestBody body = RequestBody.create(message.toByteArray(), MediaType.parse(contentType));
    builder.post(body);

    try (Response response = client.newCall(builder.build()).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        return Opamp.ServerToAgent.parseFrom(response.body().byteStream());
      }
    }
    return null;
  }
}
