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
package co.elastic.opamp.client.internal.request.websocket;

import co.elastic.opamp.client.connectivity.websocket.WebSocket;
import co.elastic.opamp.client.connectivity.websocket.WebSocketListener;
import co.elastic.opamp.client.internal.periodictask.PeriodicTaskExecutor;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestService;
import co.elastic.opamp.client.request.delay.AcceptsDelaySuggestion;
import co.elastic.opamp.client.request.delay.PeriodicDelay;
import co.elastic.opamp.client.response.Response;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public final class WebSocketRequestService implements RequestService, WebSocketListener, Runnable {
  private final WebSocket webSocket;
  private final PeriodicDelay periodicRetryDelay;
  private final AtomicBoolean retryConnectionModeEnabled = new AtomicBoolean(false);
  private final AtomicBoolean websocketRunning = new AtomicBoolean(false);
  private PeriodicTaskExecutor executor;
  private Callback callback;
  private Supplier<Request> requestSupplier;

  public static WebSocketRequestService create(
      WebSocket webSocket, PeriodicDelay periodicRetryDelay) {
    return new WebSocketRequestService(webSocket, periodicRetryDelay);
  }

  WebSocketRequestService(WebSocket webSocket, PeriodicDelay periodicRetryDelay) {
    this.webSocket = webSocket;
    this.periodicRetryDelay = periodicRetryDelay;
  }

  @Override
  public void start(Callback callback, Supplier<Request> requestSupplier) {
    this.callback = callback;
    this.requestSupplier = requestSupplier;
    if (websocketRunning.compareAndSet(false, true)) {
      webSocket.start(this);
    }
  }

  @Override
  public void sendRequest() {
    if (websocketRunning.get()) {
      doSendRequest();
    }
  }

  private void doSendRequest() {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      CodedOutputStream codedOutput = CodedOutputStream.newInstance(outputStream);
      codedOutput.writeUInt64NoTag(0);
      requestSupplier.get().getAgentToServer().writeTo(codedOutput);
      codedOutput.flush();
      webSocket.send(outputStream.toByteArray());
    } catch (IOException e) {
      callback.onRequestFailed(e);
    }
  }

  @Override
  public void stop() {
    sendRequest();
    stopWebSocket();
  }

  @Override
  public void onOpened(WebSocket webSocket) {
    disableRetryMode();
    websocketRunning.set(true);
    callback.onConnectionSuccess();
    if (retryConnectionModeEnabled.get()) {
      sendRequest();
    }
  }

  @Override
  public void onMessage(WebSocket webSocket, byte[] data) {
    try {
      Opamp.ServerToAgent serverToAgent = Opamp.ServerToAgent.parseFrom(data);

      if (serverToAgent.hasErrorResponse()) {
        handleServerError(serverToAgent.getErrorResponse());
      }

      callback.onRequestSuccess(Response.create(serverToAgent));
    } catch (InvalidProtocolBufferException e) {
      callback.onRequestFailed(e);
    }
  }

  private void handleServerError(Opamp.ServerErrorResponse errorResponse) {
    if (shouldRetry(errorResponse)) {
      Duration retryAfter = null;

      if (errorResponse.hasRetryInfo()) {
        retryAfter = Duration.ofNanos(errorResponse.getRetryInfo().getRetryAfterNanoseconds());
      }

      enableRetryMode(retryAfter);
    }
  }

  private static boolean shouldRetry(Opamp.ServerErrorResponse errorResponse) {
    return errorResponse
        .getType()
        .equals(Opamp.ServerErrorResponseType.ServerErrorResponseType_Unavailable);
  }

  private void enableRetryMode(Duration retryAfter) {
    if (retryConnectionModeEnabled.compareAndSet(false, true)) {
      stopWebSocket();
      if (retryAfter != null && periodicRetryDelay instanceof AcceptsDelaySuggestion) {
        ((AcceptsDelaySuggestion) periodicRetryDelay).suggestDelay(retryAfter);
      }
      executor = PeriodicTaskExecutor.create(periodicRetryDelay);
      executor.start(this);
    }
  }

  private void disableRetryMode() {
    if (retryConnectionModeEnabled.compareAndSet(true, false)) {
      executor.stop();
      executor = null;
    }
  }

  @Override
  public void onClosed(WebSocket webSocket) {
    websocketRunning.set(false);
  }

  @Override
  public void onFailure(WebSocket webSocket, Throwable t) {
    callback.onConnectionFailed(t);
    enableRetryMode(null);
  }

  @Override
  public void run() {
    retry();
  }

  private void retry() {
    if (retryConnectionModeEnabled.get()) {
      webSocket.start(this);
    }
  }

  private void stopWebSocket() {
    if (websocketRunning.get()) {
      webSocket.stop();
    }
  }
}
