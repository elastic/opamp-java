package co.elastic.opamp.client.request;

import opamp.proto.Opamp;

public interface MessageSender {

  void send(Opamp.AgentToServer message, Callback callback);

  interface Callback {
    void onSuccess(Opamp.ServerToAgent response);

    void onFailure(int code, String message);

    void onException(Throwable throwable);
  }
}
