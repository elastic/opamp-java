package co.elastic.opamp.client.request;

import opamp.proto.Opamp;

public interface MessageSender {

  void send(Opamp.AgentToServer message, RequestCallback callback);
}
