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

  void setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig);

  interface Callback {
    void onConnect(OpampClient client);

    void onConnectFailed(OpampClient client, Throwable throwable);

    void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse);

    void onMessage(OpampClient client, Response response);
  }
}
