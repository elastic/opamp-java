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
package co.elastic.opamp.client.internal.request.http;

import co.elastic.opamp.client.internal.periodictask.PeriodicTaskExecutor;
import co.elastic.opamp.client.request.HttpSender;
import co.elastic.opamp.client.request.HttpSender.Response;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestService;
import co.elastic.opamp.client.request.delay.AcceptsDelaySuggestion;
import co.elastic.opamp.client.request.delay.PeriodicDelay;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public final class HttpRequestService implements RequestService, Runnable {
  private final HttpSender requestSender;
  private final PeriodicTaskExecutor executor;
  private final PeriodicDelay periodicRequestDelay;
  private final PeriodicDelay periodicRetryDelay;
  private final Object runningLock = new Object();
  private Callback callback;
  private Supplier<Request> requestSupplier;
  private boolean retryModeEnabled = false;
  private boolean isRunning = false;
  private boolean isStopped = false;

  HttpRequestService(
      HttpSender requestSender,
      PeriodicTaskExecutor executor,
      PeriodicDelay periodicRequestDelay,
      PeriodicDelay periodicRetryDelay) {
    this.requestSender = requestSender;
    this.executor = executor;
    this.periodicRequestDelay = periodicRequestDelay;
    this.periodicRetryDelay = periodicRetryDelay;
  }

  public static HttpRequestService create(
      HttpSender requestSender,
      PeriodicDelay periodicRequestDelay,
      PeriodicDelay periodicRetryDelay) {
    return new HttpRequestService(
        requestSender,
        PeriodicTaskExecutor.create(periodicRequestDelay),
        periodicRequestDelay,
        periodicRetryDelay);
  }

  @Override
  public void start(Callback callback, Supplier<Request> requestSupplier) {
    synchronized (runningLock) {
      if (isStopped) {
        throw new IllegalStateException("RequestDispatcher has been stopped");
      }
      if (isRunning) {
        throw new IllegalStateException("RequestDispatcher is already running");
      }
      this.callback = callback;
      this.requestSupplier = requestSupplier;
      executor.start(this);
      isRunning = true;
    }
  }

  @Override
  public void stop() {
    synchronized (runningLock) {
      if (!isRunning || isStopped) {
        return;
      }
      isStopped = true;
      executor.executeNow();
      executor.stop();
    }
  }

  private void enableRetryMode(Duration suggestedDelay) {
    if (!retryModeEnabled) {
      retryModeEnabled = true;
      if (suggestedDelay != null && periodicRequestDelay instanceof AcceptsDelaySuggestion) {
        ((AcceptsDelaySuggestion) periodicRequestDelay).suggestDelay(suggestedDelay);
      }
      executor.setPeriodicDelay(periodicRetryDelay);
    }
  }

  private void disableRetryMode() {
    if (retryModeEnabled) {
      retryModeEnabled = false;
      executor.setPeriodicDelay(periodicRequestDelay);
    }
  }

  @Override
  public void sendRequest() {
    executor.executeNow();
  }

  @Override
  public void run() {
    doSendRequest();
  }

  private void doSendRequest() {
    try {
      Opamp.AgentToServer agentToServer = requestSupplier.get().getAgentToServer();

      try (Response response =
          requestSender
              .send(
                  new ByteArrayWriter(agentToServer.toByteArray()),
                  agentToServer.getSerializedSize())
              .get()) {
        callback.onRequestSuccess(
            co.elastic.opamp.client.response.Response.create(
                Opamp.ServerToAgent.parseFrom(response.bodyInputStream())));
      } catch (IOException e) {
        callback.onRequestFailed(e);
      }

    } catch (InterruptedException e) {
      callback.onRequestFailed(e);
    } catch (ExecutionException e) {
      callback.onRequestFailed(e.getCause());
    }
  }

  private static class ByteArrayWriter implements Consumer<OutputStream> {
    private final byte[] data;

    private ByteArrayWriter(byte[] data) {
      this.data = data;
    }

    @Override
    public void accept(OutputStream outputStream) {
      try {
        outputStream.write(data);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
