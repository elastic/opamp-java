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

import co.elastic.opamp.client.connectivity.http.HttpSender;
import co.elastic.opamp.client.connectivity.http.OkHttpSender;
import co.elastic.opamp.client.internal.OpampClientImpl;
import co.elastic.opamp.client.internal.request.RequestProvider;
import co.elastic.opamp.client.internal.request.appenders.AgentDescriptionAppender;
import co.elastic.opamp.client.internal.request.appenders.AgentDisconnectAppender;
import co.elastic.opamp.client.internal.request.appenders.AgentToServerAppenders;
import co.elastic.opamp.client.internal.request.appenders.CapabilitiesAppender;
import co.elastic.opamp.client.internal.request.appenders.EffectiveConfigAppender;
import co.elastic.opamp.client.internal.request.appenders.FlagsAppender;
import co.elastic.opamp.client.internal.request.appenders.InstanceUidAppender;
import co.elastic.opamp.client.internal.request.appenders.RemoteConfigStatusAppender;
import co.elastic.opamp.client.internal.request.appenders.SequenceNumberAppender;
import co.elastic.opamp.client.internal.request.http.HttpRequestService;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.request.delay.PeriodicDelay;
import java.time.Duration;
import opamp.proto.Anyvalue;
import opamp.proto.Opamp;

/** Builds an {@link OpampClient} instance. */
public final class HttpOpampClientBuilder {
  private HttpSender sender = OkHttpSender.create("http://localhost:4320/v1/opamp");
  private PeriodicDelay pollingIntervalDelay =
      PeriodicDelay.ofFixedDuration(Duration.ofSeconds(30));
  private PeriodicDelay retryIntervalDelay = PeriodicDelay.ofFixedDuration(Duration.ofSeconds(30));
  private final OpampClientState state = OpampClientState.create();

  /**
   * Sets an implementation of a {@link co.elastic.opamp.client.request.RequestService} to send HTTP
   * requests. The default implementation uses {@link okhttp3.OkHttpClient}.
   *
   * @param sender The HTTP request sender.
   * @return this
   */
  public HttpOpampClientBuilder setRequestSender(HttpSender sender) {
    this.sender = sender;
    return this;
  }

  /**
   * Sets the Agent's <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agenttoserverinstance_uid">instance_uid</a>
   * value. A random one is generated by default.
   *
   * @param instanceUid The AgentToServer.instance_uid value.
   * @return this
   */
  public HttpOpampClientBuilder setInstanceUid(byte[] instanceUid) {
    state.instanceUidState.set(instanceUid);
    return this;
  }

  /**
   * Sets the "service.name" attribute into the <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agentdescriptionidentifying_attributes">identifying_attributes</a>
   * field.
   *
   * @param serviceName The service name.
   * @return this
   */
  public HttpOpampClientBuilder setServiceName(String serviceName) {
    addIdentifyingAttribute("service.name", serviceName);
    return this;
  }

  /**
   * Sets the "service.namespace" attribute into the <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agentdescriptionidentifying_attributes">identifying_attributes</a>
   * field.
   *
   * @param serviceNamespace The service namespace.
   * @return this
   */
  public HttpOpampClientBuilder setServiceNamespace(String serviceNamespace) {
    addIdentifyingAttribute("service.namespace", serviceNamespace);
    return this;
  }

  /**
   * Sets the "service.version" attribute into the <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agentdescriptionidentifying_attributes">identifying_attributes</a>
   * field.
   *
   * @param serviceVersion The service version.
   * @return this
   */
  public HttpOpampClientBuilder setServiceVersion(String serviceVersion) {
    addIdentifyingAttribute("service.version", serviceVersion);
    return this;
  }

  /**
   * Sets the {@link PeriodicDelay} implementation for regular Server polling when using the HTTP
   * transport. Check out the {@link PeriodicDelay} docs for more details. By default, is set to a
   * fixed duration of 30 seconds each interval.
   *
   * @param pollingIntervalDelay The polling interval handler implementation.
   * @return this
   */
  public HttpOpampClientBuilder setPollingIntervalDelay(PeriodicDelay pollingIntervalDelay) {
    this.pollingIntervalDelay = pollingIntervalDelay;
    return this;
  }

  /**
   * Sets the {@link PeriodicDelay} implementation for retry operations when polling the Server.
   * Check out the {@link PeriodicDelay} docs for more details. By default, is set to a fixed
   * duration of 30 seconds each interval.
   *
   * @param retryIntervalDelay The retry interval handler implementation.
   * @return this
   */
  public HttpOpampClientBuilder setRetryIntervalDelay(PeriodicDelay retryIntervalDelay) {
    this.retryIntervalDelay = retryIntervalDelay;
    return this;
  }

  /**
   * Adds the AcceptsRemoteConfig and ReportsRemoteConfig capabilities to the Client so that the
   * Server can offer remote config values as explained <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agentdescriptionidentifying_attributes">here</a>.
   *
   * @return this
   */
  public HttpOpampClientBuilder enableRemoteConfig() {
    state.capabilitiesState.add(
        Opamp.AgentCapabilities.AgentCapabilities_AcceptsRemoteConfig_VALUE
            | Opamp.AgentCapabilities.AgentCapabilities_ReportsRemoteConfig_VALUE);
    return this;
  }

  /**
   * Adds the ReportsEffectiveConfig capability to the Client so that the Server expects the
   * Client's effective config report, as explained <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agentdescriptionidentifying_attributes">here</a>.
   *
   * @return this
   */
  public HttpOpampClientBuilder enableEffectiveConfigReporting() {
    state.capabilitiesState.add(
        Opamp.AgentCapabilities.AgentCapabilities_ReportsEffectiveConfig_VALUE);
    return this;
  }

  public OpampClient build() {
    AgentToServerAppenders appenders =
        new AgentToServerAppenders(
            AgentDescriptionAppender.create(state.agentDescriptionState),
            EffectiveConfigAppender.create(state.effectiveConfigState),
            RemoteConfigStatusAppender.create(state.remoteConfigStatusState),
            SequenceNumberAppender.create(state.sequenceNumberState),
            CapabilitiesAppender.create(state.capabilitiesState),
            InstanceUidAppender.create(state.instanceUidState),
            FlagsAppender.create(),
            AgentDisconnectAppender.create());
    HttpRequestService dispatcher =
        HttpRequestService.create(sender, pollingIntervalDelay, retryIntervalDelay);
    return OpampClientImpl.create(dispatcher, RequestProvider.create(appenders), state);
  }

  private void addIdentifyingAttribute(String key, String value) {
    state.agentDescriptionState.set(
        Opamp.AgentDescription.newBuilder(state.agentDescriptionState.get())
            .addIdentifyingAttributes(createKeyValue(key, value))
            .build());
  }

  private Anyvalue.KeyValue createKeyValue(String key, String value) {
    return Anyvalue.KeyValue.newBuilder()
        .setKey(key)
        .setValue(Anyvalue.AnyValue.newBuilder().setStringValue(value).build())
        .build();
  }
}
