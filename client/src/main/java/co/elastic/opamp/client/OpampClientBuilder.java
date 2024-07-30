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

import co.elastic.opamp.client.internal.OpampClientImpl;
import co.elastic.opamp.client.internal.request.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.request.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.request.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.request.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.request.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.request.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.request.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.request.visitors.SequenceNumberVisitor;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.request.handlers.IntervalHandler;
import co.elastic.opamp.client.request.impl.OkHttpRequestSender;
import java.time.Duration;
import opamp.proto.Anyvalue;
import opamp.proto.Opamp;

/** Builds an {@link OpampClient} instance. */
public final class OpampClientBuilder {
  private RequestSender sender = OkHttpRequestSender.create("http://localhost:4320/v1/opamp");
  private IntervalHandler pollingIntervalHandler = IntervalHandler.fixed(Duration.ofSeconds(30));
  private IntervalHandler retryIntervalHandler = IntervalHandler.fixed(Duration.ofSeconds(30));
  private final OpampClientState state = OpampClientState.create();

  /**
   * Sets an implementation of a {@link RequestSender} to send HTTP requests. The default
   * implementation uses {@link okhttp3.OkHttpClient}.
   *
   * @param sender The HTTP request sender.
   * @return this
   */
  public OpampClientBuilder setRequestSender(RequestSender sender) {
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
  public OpampClientBuilder setInstanceUid(byte[] instanceUid) {
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
  public OpampClientBuilder setServiceName(String serviceName) {
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
  public OpampClientBuilder setServiceNamespace(String serviceNamespace) {
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
  public OpampClientBuilder setServiceVersion(String serviceVersion) {
    addIdentifyingAttribute("service.version", serviceVersion);
    return this;
  }

  /**
   * Sets the {@link IntervalHandler} implementation for regular Server polling when using the HTTP
   * transport. Check out the {@link IntervalHandler} docs for more details. By default, is set to a
   * fixed duration of 30 seconds each interval.
   *
   * @param pollingIntervalHandler The polling interval handler implementation.
   * @return this
   */
  public OpampClientBuilder setPollingIntervalHandler(IntervalHandler pollingIntervalHandler) {
    this.pollingIntervalHandler = pollingIntervalHandler;
    return this;
  }

  /**
   * Sets the {@link IntervalHandler} implementation for retry operations when polling the Server.
   * Check out the {@link IntervalHandler} docs for more details. By default, is set to a fixed
   * duration of 30 seconds each interval.
   *
   * @param retryIntervalHandler The retry interval handler implementation.
   * @return this
   */
  public OpampClientBuilder setRetryIntervalHandler(IntervalHandler retryIntervalHandler) {
    this.retryIntervalHandler = retryIntervalHandler;
    return this;
  }

  /**
   * Adds the AcceptsRemoteConfig and ReportsRemoteConfig capabilities to the Client so that the
   * Server can offer remote config values as explained <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agentdescriptionidentifying_attributes">here</a>.
   *
   * @return this
   */
  public OpampClientBuilder enableRemoteConfig() {
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
  public OpampClientBuilder enableEffectiveConfigReporting() {
    state.capabilitiesState.add(
        Opamp.AgentCapabilities.AgentCapabilities_ReportsEffectiveConfig_VALUE);
    return this;
  }

  public OpampClient build() {
    OpampClientVisitors visitors =
        new OpampClientVisitors(
            AgentDescriptionVisitor.create(state.agentDescriptionState),
            EffectiveConfigVisitor.create(state.effectiveConfigState),
            RemoteConfigStatusVisitor.create(state.remoteConfigStatusState),
            SequenceNumberVisitor.create(state.sequenceNumberState),
            CapabilitiesVisitor.create(state.capabilitiesState),
            InstanceUidVisitor.create(state.instanceUidState),
            FlagsVisitor.create(),
            AgentDisconnectVisitor.create());
    return OpampClientImpl.create(
        sender, visitors, state, pollingIntervalHandler, retryIntervalHandler);
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
