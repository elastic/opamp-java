package co.elastic.opamp.client.internal.scheduler;

import opamp.proto.Opamp;

public interface ResponseHandler {
  void handleResponse(Opamp.ServerToAgent serverToAgent);

  void handleError(Throwable t);
}
