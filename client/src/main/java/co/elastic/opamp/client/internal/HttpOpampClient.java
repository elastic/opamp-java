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
import co.elastic.opamp.client.internal.request.RequestBuilder;
import co.elastic.opamp.client.internal.request.RequestDispatcher;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.observer.Observable;
import co.elastic.opamp.client.internal.state.observer.Observer;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.http.RequestSender;
import co.elastic.opamp.client.request.http.handlers.IntervalHandler;
import co.elastic.opamp.client.response.MessageData;
import com.google.protobuf.ByteString;
import java.time.Duration;
import opamp.proto.Opamp;

public final class HttpOpampClient implements OpampClient, Observer, Runnable {
  private final RequestSender sender;
  private final RequestDispatcher dispatcher;
  private final RequestBuilder requestBuilder;
  private final OpampClientState state;
  private final Object runningLock = new Object();
  private Callback callback;
  private boolean isRunning;
  private boolean isStopped;

  public static HttpOpampClient create(
      RequestSender sender,
      OpampClientVisitors visitors,
      OpampClientState state,
      IntervalHandler pollingInterval,
      IntervalHandler retryInterval) {
    RequestBuilder requestBuilder = RequestBuilder.create(visitors);
    RequestDispatcher dispatcher = RequestDispatcher.create(pollingInterval, retryInterval);
    return new HttpOpampClient(sender, dispatcher, requestBuilder, state);
  }

  HttpOpampClient(
      RequestSender sender,
      RequestDispatcher dispatcher,
      RequestBuilder requestBuilder,
      OpampClientState state) {
    this.sender = sender;
    this.dispatcher = dispatcher;
    this.requestBuilder = requestBuilder;
    this.state = state;
  }

  @Override
  public void start(Callback callback) {
    synchronized (runningLock) {
      if (!isRunning) {
        isRunning = true;
        this.callback = callback;
        observeStatusChange();
        dispatcher.start(this);
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
        requestBuilder.stop();
        dispatcher.stop();
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

  private void onConnectionSuccess(Opamp.ServerToAgent response) {
    state.sequenceNumberState.increment();
    callback.onConnect(this);
    if (dispatcher.isRetryModeEnabled()) dispatcher.disableRetryMode();
    if (response == null) return;

    handleResponse(response);
  }

  private void onConnectionError(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
    if (!dispatcher.isRetryModeEnabled()) dispatcher.enableRetryMode(null);
  }

  private void handleResponse(Opamp.ServerToAgent response) {
    if (response.hasErrorResponse()) {
      Opamp.ServerErrorResponse errorResponse = response.getErrorResponse();
      handleErrorResponse(errorResponse);
      callback.onErrorResponse(this, errorResponse);
    }
    long reportFullState = Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE;
    if ((response.getFlags() & reportFullState) == reportFullState) {
      requestBuilder.disableCompression();
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
        dispatcher.enableRetryMode(Duration.ofNanos(retryAfterNanoseconds));
      } else {
        dispatcher.enableRetryMode(null);
      }
    }
  }

  @Override
  public void run() {
    Request request = requestBuilder.buildAndReset();

    RequestSender.Response response = sender.send(request);

    if (isStopped) {
      return;
    }

    if (response instanceof RequestSender.Response.Success) {
      onConnectionSuccess(((RequestSender.Response.Success) response).data);
    } else if (response instanceof RequestSender.Response.Error) {
      onConnectionError(((RequestSender.Response.Error) response).throwable);
    } else {
      throw new IllegalStateException("Unexpected response: " + response);
    }
  }

  @Override
  public void update(Observable observable) {
    // There was an agent status change.
    dispatcher.tryDispatchNow();
  }

  private void observeStatusChange() {
    state.agentDescriptionState.addObserver(this);
    state.effectiveConfigState.addObserver(this);
    state.remoteConfigStatusState.addObserver(this);
    state.capabilitiesState.addObserver(this);
    state.instanceUidState.addObserver(this);
  }
}
