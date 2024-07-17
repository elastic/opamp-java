package co.elastic.opamp.client.request;

import com.google.auto.value.AutoValue;
import opamp.proto.Opamp;

@AutoValue
public abstract class Request {
  public abstract Opamp.AgentToServer getAgentToServer();

  public static Request create(Opamp.AgentToServer agentToServer) {
    return new AutoValue_Request(agentToServer);
  }
}
