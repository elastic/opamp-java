package co.elastic.opamp.client.connectivity.websocket;

public interface WebSocketListener {
  void onOpened(WebSocket webSocket);

  void onMessage(WebSocket webSocket, byte[] data);

  void onClosed(WebSocket webSocket);

  void onFailure(WebSocket webSocket, Throwable t);
}
