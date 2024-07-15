package co.elastic.opamp.client.response;

import com.google.auto.value.AutoValue;
import opamp.proto.Opamp;

@AutoValue
public abstract class Response {
  public abstract Opamp.AgentRemoteConfig getRemoteConfig();

  public static Builder builder() {
    return new AutoValue_Response.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setRemoteConfig(Opamp.AgentRemoteConfig remoteConfig);

    public abstract Response build();
  }
}
