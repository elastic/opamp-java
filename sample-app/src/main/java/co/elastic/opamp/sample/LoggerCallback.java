package co.elastic.opamp.sample;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.response.MessageData;
import java.util.logging.Logger;
import opamp.proto.Opamp;

public class LoggerCallback implements OpampClient.Callback {
  private final Logger logger = Logger.getLogger(getClass().getSimpleName());

  @Override
  public void onConnect(OpampClient client) {
    logger.info("Connected to Opamp, client: " + client);
  }

  @Override
  public void onConnectFailed(OpampClient client, Throwable throwable) {
    logger.info("Connection failed: " + throwable);
  }

  @Override
  public void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse) {
    logger.info("Server error: " + errorResponse);
  }

  @Override
  public void onMessage(OpampClient client, MessageData messageData) {
    logger.info("Message received: " + messageData);
  }
}
