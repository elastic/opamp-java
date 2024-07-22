package co.elastic.opamp.sample;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.response.MessageData;
import java.util.concurrent.CountDownLatch;
import opamp.proto.Opamp;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    new Thread(
            () -> {
              OpampClient client =
                  OpampClient.builder()
                      .setServiceName("My Service")
                      .build(
                          new OpampClient.Callback() {
                            @Override
                            public void onConnect(OpampClient client) {
                              client.stop();
                              latch.countDown();
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
            })
        .start();

    latch.await();
  }
}
