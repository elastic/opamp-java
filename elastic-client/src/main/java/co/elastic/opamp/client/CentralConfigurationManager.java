package co.elastic.opamp.client;

import co.elastic.opamp.client.response.MessageData;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import opamp.proto.Opamp;

public final class CentralConfigurationManager implements OpampClient.Callback {
  private final OpampClient client;
  private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
  private final Logger logger = Logger.getLogger(CentralConfigurationManager.class.getName());
  private CentralConfigurationProcessor processor;

  CentralConfigurationManager(OpampClient client) {
    this.client = client;
  }

  public void start(CentralConfigurationProcessor processor) {
    this.processor = processor;
    client.start(this);
  }

  public void stop() {
    client.stop();
  }

  @Override
  public void onMessage(OpampClient client, MessageData messageData) {
    Opamp.AgentRemoteConfig remoteConfig = messageData.getRemoteConfig();
    if (remoteConfig != null) {
      processRemoteConfig(client, remoteConfig);
    }
  }

  private void processRemoteConfig(OpampClient client, Opamp.AgentRemoteConfig remoteConfig) {
    Map<String, Opamp.AgentConfigFile> configMapMap = remoteConfig.getConfig().getConfigMapMap();
    Opamp.AgentConfigFile centralConfig = configMapMap.get("");
    if (centralConfig != null) {
      CentralConfigurationProcessor.Result result =
          processor.process(parseCentralConfiguration(centralConfig.getBody()));

      Opamp.RemoteConfigStatuses status =
          (result == CentralConfigurationProcessor.Result.APPLIED)
              ? Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLIED
              : Opamp.RemoteConfigStatuses.RemoteConfigStatuses_FAILED;

      client.setRemoteConfigStatus(getRemoteConfigStatus(status));
    }
  }

  private static Opamp.RemoteConfigStatus getRemoteConfigStatus(Opamp.RemoteConfigStatuses status) {
    return Opamp.RemoteConfigStatus.newBuilder().setStatus(status).build();
  }

  private Map<String, String> parseCentralConfiguration(ByteString centralConfig) {
    try {
      JsonReader<Object> reader = dslJson.newReader(centralConfig.toByteArray());
      reader.startObject();
      return Collections.unmodifiableMap(MapConverter.deserialize(reader));
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not parse central configuration.", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public void onConnect(OpampClient client) {}

  @Override
  public void onConnectFailed(OpampClient client, Throwable throwable) {}

  @Override
  public void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse) {}
}
