package co.elastic.opamp.client.request;

import opamp.proto.Opamp;

public interface RequestCallback {
  void onSuccess(Opamp.ServerToAgent response);

  void onFailure(int code, String message);

  void onException(Throwable throwable);
}
