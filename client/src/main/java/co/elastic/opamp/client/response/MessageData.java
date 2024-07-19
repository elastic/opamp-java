package co.elastic.opamp.client.response;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import opamp.proto.Opamp;

@AutoValue
public abstract class MessageData {
  @Nullable
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
