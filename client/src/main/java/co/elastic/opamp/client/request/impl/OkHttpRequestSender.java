package co.elastic.opamp.client.request.impl;

import co.elastic.opamp.client.request.HttpErrorException;
import co.elastic.opamp.client.request.RequestSender;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import opamp.proto.Opamp;

public class OkHttpRequestSender implements RequestSender {
  private final OkHttpClient client;
  private final String url;

  public static OkHttpRequestSender create(String url) {
    return create(new OkHttpClient(), url);
  }

  public static OkHttpRequestSender create(OkHttpClient client, String url) {
    return new OkHttpRequestSender(client, url);
  }

  private OkHttpRequestSender(OkHttpClient client, String url) {
    this.client = client;
    this.url = url;
  }

  @Override
  public void send(Opamp.AgentToServer message, RequestSender.Callback callback) {
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
        callback.onError(new HttpErrorException(response.code(), response.message()));
      }
    } catch (IOException e) {
      callback.onError(e);
    }
  }
}