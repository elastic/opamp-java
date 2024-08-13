package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import opamp.proto.Opamp;

public class WebSocketOpampClient implements OpampClient {
  @Override
  public void start(Callback callback) {}

  @Override
  public void stop() {}

  @Override
  public void setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus) {}

  @Override
  public void setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig) {}
}
