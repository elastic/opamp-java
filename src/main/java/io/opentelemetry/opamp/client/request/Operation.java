package io.opentelemetry.opamp.client.request;

import java.io.IOException;
import opamp.proto.Opamp;

public interface Operation {
  Opamp.ServerToAgent sendMessage(Opamp.AgentToServer message) throws IOException;
}
