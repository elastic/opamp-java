package io.opentelemetry.opamp;

import io.opentelemetry.opamp.client.OpampClient;
import io.opentelemetry.opamp.client.requests.OpampService;

public class Main {
  public static void main(String[] args) {
    OpampService opampService = OpampService.create("http://localhost:4320/v1/opamp");
    OpampClient client = OpampClient.create(opampService, "some.name", "0.0.1");

    client.start();
    client.stop();
  }
}
