package co.elastic.opamp.client.internal.scheduler;

import opamp.proto.Opamp;

public final class Message {
  public final Opamp.AgentToServer agentToServer;

  public Message(Opamp.AgentToServer agentToServer) {
    this.agentToServer = agentToServer;
  }
}
