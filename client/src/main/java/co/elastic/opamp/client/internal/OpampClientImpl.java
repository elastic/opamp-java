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
import co.elastic.opamp.client.internal.request.RequestDispatcher;
import co.elastic.opamp.client.internal.request.RequestListener;
import co.elastic.opamp.client.internal.request.RequestProvider;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.observer.Observable;
import co.elastic.opamp.client.internal.state.observer.Observer;
import co.elastic.opamp.client.response.MessageData;
import co.elastic.opamp.client.response.Response;
import com.google.protobuf.ByteString;
import java.time.Duration;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, Observer, RequestListener {
  private final RequestDispatcher requestDispatcher;
  private final RequestProvider requestProvider;
  private final OpampClientState state;
  private final Object runningLock = new Object();
  private Callback callback;
  private boolean isRunning;
  private boolean isStopped;

  public static OpampClientImpl create(
      RequestDispatcher requestDispatcher,
      RequestProvider requestProvider,
      OpampClientState state) {
    return new OpampClientImpl(requestDispatcher, requestProvider, state);
  }

  private OpampClientImpl(
      RequestDispatcher requestDispatcher,
      RequestProvider requestProvider,
      OpampClientState state) {
    this.requestDispatcher = requestDispatcher;
    this.requestProvider = requestProvider;
    this.state = state;
  }

  @Override
  public void start(Callback callback) {
    synchronized (runningLock) {
      if (!isRunning) {
        isRunning = true;
        this.callback = callback;
        observeStatusChange();
        requestDispatcher.start(this);
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
        requestProvider.stop();
        requestDispatcher.stop();
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
  public void onSuccessfulRequest(Response response) {
    state.sequenceNumberState.increment();
    callback.onConnect(this);
    if (requestDispatcher.isRetryModeEnabled()) requestDispatcher.disableRetryMode();
    if (response == null) return;

    handleResponse(response.getServerToAgent());
  }

  @Override
  public void onFailedRequest(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
    if (!requestDispatcher.isRetryModeEnabled()) requestDispatcher.enableRetryMode(null);
  }

  private void handleResponse(Opamp.ServerToAgent response) {
    if (response.hasErrorResponse()) {
      Opamp.ServerErrorResponse errorResponse = response.getErrorResponse();
      handleErrorResponse(errorResponse);
      callback.onErrorResponse(this, errorResponse);
    }
    long reportFullState = Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE;
    if ((response.getFlags() & reportFullState) == reportFullState) {
      requestProvider.disableCompression();
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

  private void handleAgentIdentification(Opamp.ServerToAgent response) {
    if (response.hasAgentIdentification()) {
      ByteString newInstanceUid = response.getAgentIdentification().getNewInstanceUid();
      if (!newInstanceUid.isEmpty()) {
        state.instanceUidState.set(newInstanceUid.toByteArray());
      }
    }
  }

  private void handleErrorResponse(Opamp.ServerErrorResponse errorResponse) {
    if (errorResponse.getType()
        == Opamp.ServerErrorResponseType.ServerErrorResponseType_Unavailable) {
      if (errorResponse.hasRetryInfo()) {
        long retryAfterNanoseconds = errorResponse.getRetryInfo().getRetryAfterNanoseconds();
        requestDispatcher.enableRetryMode(Duration.ofNanos(retryAfterNanoseconds));
      } else {
        requestDispatcher.enableRetryMode(null);
      }
    }
  }

  @Override
  public void update(Observable observable) {
    // There was an agent status change.
    requestDispatcher.tryDispatchNow();
  }

  private void observeStatusChange() {
    state.agentDescriptionState.addObserver(this);
    state.effectiveConfigState.addObserver(this);
    state.remoteConfigStatusState.addObserver(this);
    state.capabilitiesState.addObserver(this);
    state.instanceUidState.addObserver(this);
  }
}
