package co.elastic.opamp.sample;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.response.MessageData;
import opamp.proto.Opamp;

public class Main {
  public static void main(String[] args) {
    OpampClient client =
        OpampClient.builder()
            .build(
                new OpampClient.Callback() {
                  @Override
                  public void onConnect(OpampClient client) {
                    client.stop();
                  }

                  @Override
                  public void onConnectFailed(OpampClient client, Throwable throwable) {}

                  @Override
                  public void onErrorResponse(
                      OpampClient client, Opamp.ServerErrorResponse errorResponse) {}

                  @Override
                  public void onMessage(OpampClient client, MessageData messageData) {}
                });

    client.start();
  }
}
