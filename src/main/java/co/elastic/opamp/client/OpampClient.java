package co.elastic.opamp.client;

import co.elastic.opamp.client.internal.OpampClientBuilder;
import co.elastic.opamp.client.response.Response;
import opamp.proto.Opamp;

public interface OpampClient {

  static OpampClientBuilder builder() {
    return new OpampClientBuilder();
  }

  void start();

  void stop();

  void setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus);

  interface Callback {
    void onMessage(OpampClient client, Response response);
  }
}
