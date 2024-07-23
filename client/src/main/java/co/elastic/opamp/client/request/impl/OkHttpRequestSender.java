package co.elastic.opamp.client.request.impl;

import co.elastic.opamp.client.request.HttpErrorException;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestSender;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import opamp.proto.Opamp;

/**
 * {@link RequestSender} implementation that uses {@link okhttp3.OkHttpClient} to send the request.
 */
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
  public Response send(Request request) {
    okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
    String contentType = "application/x-protobuf";
    builder.addHeader("Content-Type", contentType);

    RequestBody body =
        RequestBody.create(request.getAgentToServer().toByteArray(), MediaType.parse(contentType));
    builder.post(body);

    try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
      if (response.isSuccessful()) {
        if (response.body() != null) {
          Opamp.ServerToAgent serverToAgent =
              Opamp.ServerToAgent.parseFrom(response.body().byteStream());
          return Response.success(serverToAgent);
        }
      } else {
        return Response.error(new HttpErrorException(response.code(), response.message()));
      }
    } catch (IOException e) {
      return Response.error(e);
    }

    return Response.error(new IllegalStateException());
  }
}
