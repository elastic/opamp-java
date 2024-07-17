package co.elastic.opamp.client.request;

import opamp.proto.Opamp;

public interface RequestSender {

  void send(Opamp.AgentToServer message, Callback callback);

  interface Callback {
    void onSuccess(Opamp.ServerToAgent response);

    void onError(Throwable throwable);
  }
}
