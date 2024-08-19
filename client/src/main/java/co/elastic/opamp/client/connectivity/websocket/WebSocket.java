package co.elastic.opamp.client.connectivity.websocket;

import co.elastic.opamp.client.request.Request;

public interface WebSocket {
  void start(WebSocketListener listener);

  void send(Request request);

  void stop();
}
