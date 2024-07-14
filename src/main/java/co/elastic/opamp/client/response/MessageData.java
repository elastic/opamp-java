package co.elastic.opamp.client.response;

import com.google.auto.value.AutoValue;
import opamp.proto.Opamp;

@AutoValue
public abstract class MessageData {
  public abstract Opamp.AgentRemoteConfig getRemoteConfig();

  public static Builder builder() {
    return new AutoValue_MessageData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setRemoteConfig(Opamp.AgentRemoteConfig remoteConfig);

    public abstract MessageData build();
  }
}
