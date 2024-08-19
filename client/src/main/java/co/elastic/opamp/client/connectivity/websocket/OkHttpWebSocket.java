package co.elastic.opamp.client.connectivity.websocket;

import co.elastic.opamp.client.request.Request;
import okhttp3.OkHttpClient;

public class OkHttpWebSocket implements WebSocket {
  private final OkHttpClient client;
  private final String url;

  public static OkHttpWebSocket create(String url) {
    OkHttpClient client = new OkHttpClient();
    return new OkHttpWebSocket(client, url);
  }

  public OkHttpWebSocket(OkHttpClient client, String url) {
    this.client = client;
    this.url = url;
  }

  @Override
  public void start(WebSocketListener listener) {

  }

  @Override
  public void send(Request request) {}

  @Override
  public void stop() {}
}
