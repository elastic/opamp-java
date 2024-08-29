package co.elastic.opamp.sample;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.response.MessageData;
import java.util.logging.Logger;
import opamp.proto.Opamp;

public class MainWebsocket {
  private static final Logger logger = Logger.getLogger(MainWebsocket.class.getName());

  public static void main(String[] args) {
    OpampClient client = OpampClient.httpBuilder().enableRemoteConfig().build();

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
}
