/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.opamp.client;

import co.elastic.opamp.client.request.handlers.IntervalHandler;
import co.elastic.opamp.client.request.impl.OkHttpRequestSender;
import co.elastic.opamp.client.response.MessageData;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import opamp.proto.Opamp;

public final class CentralConfigurationManagerImpl
    implements CentralConfigurationManager, OpampClient.Callback {
  private final OpampClient client;
  private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
  private final Logger logger = Logger.getLogger(CentralConfigurationManagerImpl.class.getName());
  private CentralConfigurationProcessor processor;

  CentralConfigurationManagerImpl(OpampClient client) {
    this.client = client;
  }

  @Override
  public void start(CentralConfigurationProcessor processor) {
    this.processor = processor;
    client.start(this);
  }

  @Override
  public void stop() {
    client.stop();
  }

  @Override
  public void onMessage(OpampClient client, MessageData messageData) {
    logger.log(Level.FINEST, "onMessage({}, {})", new Object[] {client, messageData});
    Opamp.AgentRemoteConfig remoteConfig = messageData.getRemoteConfig();
    if (remoteConfig != null) {
      processRemoteConfig(client, remoteConfig);
    }
  }

  private void processRemoteConfig(OpampClient client, Opamp.AgentRemoteConfig remoteConfig) {
    Map<String, Opamp.AgentConfigFile> configMapMap = remoteConfig.getConfig().getConfigMapMap();
    Opamp.AgentConfigFile centralConfig = configMapMap.get("");
    if (centralConfig != null) {
      Map<String, String> configuration = parseCentralConfiguration(centralConfig.getBody());
      Opamp.RemoteConfigStatuses status;

      if (configuration != null) {
        CentralConfigurationProcessor.Result result = processor.process(configuration);
        status =
            (result == CentralConfigurationProcessor.Result.SUCCESS)
                ? Opamp.RemoteConfigStatuses.RemoteConfigStatuses_APPLIED
                : Opamp.RemoteConfigStatuses.RemoteConfigStatuses_FAILED;
      } else {
        status = Opamp.RemoteConfigStatuses.RemoteConfigStatuses_FAILED;
      }

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
      return null;
    }
  }

  @Override
  public void onConnect(OpampClient client) {
    logger.log(Level.FINEST, "onConnect({})", client);
  }

  @Override
  public void onConnectFailed(OpampClient client, Throwable throwable) {
    logger.log(Level.FINEST, "onConnect({}, {})", new Object[] {client, throwable});
  }

  @Override
  public void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse) {
    logger.log(Level.FINEST, "onErrorResponse({}, {})", new Object[] {client, errorResponse});
  }

  public static class Builder {
    private String serviceName;
    private String serviceNamespace;
    private String serviceVersion;
    private String configurationEndpoint;
    private Duration pollingInterval;

    Builder() {}

    public Builder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder setServiceNamespace(String serviceNamespace) {
      this.serviceNamespace = serviceNamespace;
      return this;
    }

    public Builder setServiceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
      return this;
    }

    public Builder setConfigurationEndpoint(String configurationEndpoint) {
      this.configurationEndpoint = configurationEndpoint;
      return this;
    }

    public Builder setPollingInterval(Duration pollingInterval) {
      this.pollingInterval = pollingInterval;
      return this;
    }

    public CentralConfigurationManagerImpl build() {
      OpampClientBuilder builder = OpampClient.builder();
      if (serviceName != null) {
        builder.setServiceName(serviceName);
      }
      if (serviceNamespace != null) {
        builder.setServiceNamespace(serviceNamespace);
      }
      if (serviceVersion != null) {
        builder.setServiceVersion(serviceVersion);
      }
      if (configurationEndpoint != null) {
        builder.setRequestSender(OkHttpRequestSender.create(configurationEndpoint));
      }
      if (pollingInterval != null) {
        builder.setPollingIntervalHandler(IntervalHandler.fixed(pollingInterval));
      }
      return new CentralConfigurationManagerImpl(builder.build());
    }
  }
}
