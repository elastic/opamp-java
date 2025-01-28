package co.elastic.opamp.sample;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.OpampClientBuilder;
import co.elastic.opamp.client.connectivity.http.OkHttpSender;
import co.elastic.opamp.client.connectivity.websocket.OkHttpWebSocket;
import co.elastic.opamp.client.request.service.HttpRequestService;
import co.elastic.opamp.client.request.service.WebSocketRequestService;
import co.elastic.opamp.client.response.MessageData;
import java.util.logging.Logger;
import opamp.proto.Opamp;
import org.jetbrains.annotations.NotNull;

public class GenericExample {
  private static final Logger logger = Logger.getLogger(GenericExample.class.getName());

  public static void main(String[] args) {
    OpampClient client =
        createWebsocketBuilder()
            .enableRemoteConfig()
            .enableEffectiveConfigReporting()
            .setEffectiveConfigState(new MyEffectiveConfigState())
            .build();

    client.start(
        new OpampClient.Callback() {
          @Override
          public void onConnect(OpampClient client) {
            logger.info("Client connected");
          }

          @Override
          public void onConnectFailed(OpampClient client, Throwable throwable) {
            logger.info("Client connect failed: " + throwable);
          }

          @Override
          public void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse) {
            logger.info("Server error: " + errorResponse);
          }

          @Override
          public void onMessage(OpampClient client, MessageData messageData) {
            logger.info("Message received: " + messageData);
          }
        });
  }

  @NotNull
  private static OpampClientBuilder createWebsocketBuilder() {
    return OpampClient.builder()
        .setRequestService(
            WebSocketRequestService.create(OkHttpWebSocket.create("ws://localhost:4320/v1/opamp")));
  }

  @NotNull
  private static OpampClientBuilder createHttpBuilder() {
    return OpampClient.builder()
        .setRequestService(
            HttpRequestService.create(OkHttpSender.create("http://localhost:4320/v1/opamp")));
  }
}
