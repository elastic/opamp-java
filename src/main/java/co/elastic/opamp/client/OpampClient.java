package co.elastic.opamp.client;

import co.elastic.opamp.client.internal.OpampClientBuilder;
import co.elastic.opamp.client.response.MessageData;

public interface OpampClient {

  static OpampClientBuilder builder() {
    return new OpampClientBuilder();
  }

  void start();

  void stop();

  interface Callback {
    void onMessage(OpampClient client, MessageData messageData);
  }
}
