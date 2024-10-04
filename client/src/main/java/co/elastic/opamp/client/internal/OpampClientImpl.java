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
package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.request.fields.FieldType;
import co.elastic.opamp.client.internal.request.fields.appenders.AgentToServerAppenders;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.request.fields.FieldStateChangeListener;
import co.elastic.opamp.client.internal.request.fields.FieldStateObserver;
import co.elastic.opamp.client.state.observer.Observable;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestService;
import co.elastic.opamp.client.response.MessageData;
import co.elastic.opamp.client.response.Response;
import com.google.protobuf.ByteString;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public final class OpampClientImpl
    implements OpampClient, FieldStateChangeListener, RequestService.Callback, Supplier<Request>{
  private final RequestService requestService;
  private final AgentToServerAppenders appenders;
  private final OpampClientState state;
  private final Object runningLock = new Object();
  private Callback callback;
  private boolean isRunning;
  private boolean isStopped;

  public static OpampClientImpl create(
      RequestService requestService, AgentToServerAppenders appenders, OpampClientState state) {
    return new OpampClientImpl(requestService, appenders, state);
  }

  private OpampClientImpl(
      RequestService requestService, AgentToServerAppenders appenders, OpampClientState state) {
    this.requestService = requestService;
    this.appenders = appenders;
    this.state = state;
  }

  @Override
  public void start(Callback callback) {
    synchronized (runningLock) {
      if (!isRunning) {
        isRunning = true;
        this.callback = callback;
        observeStateChange();
        requestService.start(this, this);
        requestService.sendRequest();
      } else {
        throw new IllegalStateException("The client has already been started");
      }
    }
  }

  @Override
  public void stop() {
    synchronized (runningLock) {
      if (!isRunning) {
        throw new IllegalStateException("The client has not been started");
      }
      if (!isStopped) {
        isStopped = true;
        prepareDisconnectRequest();
        requestService.stop();
      } else {
        throw new IllegalStateException("The client has already been stopped");
      }
    }
  }

  @Override
  public void setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus) {
    state.remoteConfigStatusState.set(remoteConfigStatus);
  }

  @Override
  public void setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig) {
    state.effectiveConfigState.set(effectiveConfig);
  }

  @Override
  public void onConnectionSuccess() {
    callback.onConnect(this);
  }

  @Override
  public void onConnectionFailed(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
  }

  @Override
  public void onRequestSuccess(Response response) {
    state.sequenceNumberState.increment();
    if (response == null) return;

    handleResponsePayload(response.getServerToAgent());
  }

  @Override
  public void onRequestFailed(Throwable throwable) {}

  private void handleResponsePayload(Opamp.ServerToAgent response) {
    if (response.hasErrorResponse()) {
      Opamp.ServerErrorResponse errorResponse = response.getErrorResponse();
      callback.onErrorResponse(this, errorResponse);
    }
    long reportFullState = Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE;
    if ((response.getFlags() & reportFullState) == reportFullState) {
      disableCompression();
    }
    handleAgentIdentification(response);

    boolean notifyOnMessage = false;
    MessageData.Builder messageBuilder = MessageData.builder();

    if (response.hasRemoteConfig()) {
      notifyOnMessage = true;
      messageBuilder.setRemoteConfig(response.getRemoteConfig());
    }

    if (notifyOnMessage) {
      callback.onMessage(this, messageBuilder.build());
    }
  }

  private void disableCompression() {
    appenders.asList().
  }

  private void prepareDisconnectRequest() {
    appenders.agentDisconnectAppender.enable();
  }

  private void handleAgentIdentification(Opamp.ServerToAgent response) {
    if (response.hasAgentIdentification()) {
      ByteString newInstanceUid = response.getAgentIdentification().getNewInstanceUid();
      if (!newInstanceUid.isEmpty()) {
        state.instanceUidState.set(newInstanceUid.toByteArray());
      }
    }
  }

  @Override
  public void onStateForFieldChanged(FieldType fieldType) {
    requestService.sendRequest();
  }

  @Override
  public Request get() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    appenders.asList().forEach(appender -> appender.appendTo(builder));
    return Request.create(builder.build());
  }

  private void observeStateChange() {
    addStateForFieldObserver(state.agentDescriptionState, FieldType.AGENT_DESCRIPTION);
    addStateForFieldObserver(state.effectiveConfigState, FieldType.EFFECTIVE_CONFIG);
    addStateForFieldObserver(state.remoteConfigStatusState, FieldType.REMOTE_CONFIG_STATUS);
    addStateForFieldObserver(state.capabilitiesState, FieldType.CAPABILITIES);
    addStateForFieldObserver(state.instanceUidState, FieldType.INSTANCE_UID);
  }

  private void addStateForFieldObserver(Observable observable, FieldType fieldType) {
    observable.addObserver(new FieldStateObserver(this, fieldType));
  }
}
