package co.elastic.opamp.client.internal.dispatcher;

import opamp.proto.Opamp;

public interface ResponseHandler {
  void handleSuccess(Opamp.ServerToAgent serverToAgent);

  void handleError(Throwable throwable);
}
