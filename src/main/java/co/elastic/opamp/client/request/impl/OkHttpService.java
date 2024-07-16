package co.elastic.opamp.client.request.impl;

import co.elastic.opamp.client.request.RequestCallback;
import co.elastic.opamp.client.request.Service;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import opamp.proto.Opamp;

public class OkHttpService implements Service {
  private final OkHttpClient client;
  private final String url;

  public static OkHttpService create(String url) {
    return create(new OkHttpClient(), url);
  }

  public static OkHttpService create(OkHttpClient client, String url) {
    return new OkHttpService(client, url);
  }

  private OkHttpService(OkHttpClient client, String url) {
    this.client = client;
    this.url = url;
  }

  @Override
  public void sendMessage(Opamp.AgentToServer message, RequestCallback callback) {
    Request.Builder builder = new Request.Builder().url(url);
    String contentType = "application/x-protobuf";
    builder.addHeader("Content-Type", contentType);

    RequestBody body = RequestBody.create(message.toByteArray(), MediaType.parse(contentType));
    builder.post(body);

    try (Response response = client.newCall(builder.build()).execute()) {
      if (response.isSuccessful()) {
        if (response.body() != null) {
          Opamp.ServerToAgent serverToAgent =
              Opamp.ServerToAgent.parseFrom(response.body().byteStream());
          callback.onSuccess(serverToAgent);
        }
      } else {
        callback.onFailure(response.code(), response.message());
      }
    } catch (IOException e) {
      callback.onException(e);
    }
  }
}
