package co.elastic.opamp.client.request;

import com.google.auto.value.AutoValue;
import opamp.proto.Opamp;

/** Wrapper class for "AgentToServer" request body. */
@AutoValue
public abstract class Request {
  public abstract Opamp.AgentToServer getAgentToServer();

  public static Request create(Opamp.AgentToServer agentToServer) {
    return new AutoValue_Request(agentToServer);
  }
}
