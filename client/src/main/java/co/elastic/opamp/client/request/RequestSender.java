package co.elastic.opamp.client.request;

import opamp.proto.Opamp;

public interface RequestSender {

  Response send(Opamp.AgentToServer message);

  interface Response {
    static Response success(Opamp.ServerToAgent data) {
      return new Response.Success(data);
    }

    static Response error(Throwable throwable) {
      return new Error(throwable);
    }

    final class Success implements Response {
      public final Opamp.ServerToAgent data;

      private Success(Opamp.ServerToAgent data) {
        this.data = data;
      }
    }

    final class Error implements Response {
      public final Throwable throwable;

      private Error(Throwable throwable) {
        this.throwable = throwable;
      }
    }
  }
}
