package io.opentelemetry.opamp;

import io.opentelemetry.opamp.client.OpampClient;
import io.opentelemetry.opamp.client.request.Operation;

public class Main {
  public static void main(String[] args) {
    Operation operation = Operation.create("http://localhost:4321/v1/opamp/");
    OpampClient client = OpampClient.create(operation, "some.name", "0.0.1");

    client.reportStatus();
    client.disconnect();
  }
}
