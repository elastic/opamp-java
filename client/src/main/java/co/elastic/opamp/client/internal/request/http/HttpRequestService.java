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

import co.elastic.opamp.client.connectivity.http.handlers.IntervalHandler;
import co.elastic.opamp.client.internal.request.http.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.http.handlers.sleep.ThreadSleepHandler;
import co.elastic.opamp.client.internal.request.http.handlers.sleep.impl.FixedThreadSleepHandler;
import co.elastic.opamp.client.request.HttpRequestSender;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestService;
import co.elastic.opamp.client.response.Response;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class HttpRequestService implements RequestService, Runnable {
  private final HttpRequestSender requestSender;
  private final ExecutorService executor;
  private final DualIntervalHandler requestInterval;
  private final ThreadSleepHandler threadSleepHandler;
  private final Object runningLock = new Object();
  private Callback callback;
  private Supplier<Request> requestSupplier;
  private boolean retryModeEnabled = false;
  private boolean isRunning = false;
  private boolean isStopped = false;

  HttpRequestService(
      HttpRequestSender requestSender,
      ExecutorService executor,
      DualIntervalHandler requestInterval,
      ThreadSleepHandler threadSleepHandler) {
    this.requestSender = requestSender;
    this.executor = executor;
    this.requestInterval = requestInterval;
    this.threadSleepHandler = threadSleepHandler;
  }

  public static HttpRequestService create(
      HttpRequestSender requestSender,
      IntervalHandler pollingInterval,
      IntervalHandler retryInterval) {
    return new HttpRequestService(
        requestSender,
        Executors.newSingleThreadExecutor(),
        DualIntervalHandler.of(pollingInterval, retryInterval),
        FixedThreadSleepHandler.of(Duration.ofSeconds(1)));
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
      requestInterval.startNext();
      executor.execute(this);
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
      threadSleepHandler.awakeOrIgnoreNextSleep();
    }
  }

  private void enableRetryMode(Duration suggestedInterval) {
    if (!retryModeEnabled) {
      retryModeEnabled = true;
      requestInterval.switchToSecondary();
      requestInterval.reset();
    }
    if (suggestedInterval != null) {
      requestInterval.suggestNextInterval(suggestedInterval);
    }
  }

  private void disableRetryMode() {
    if (retryModeEnabled) {
      retryModeEnabled = false;
      requestInterval.switchToMain();
      requestInterval.reset();
    }
  }

  private void tryDispatchNow() {
    if (requestInterval.fastForward()) {
      threadSleepHandler.awakeOrIgnoreNextSleep();
    }
  }

  @Override
  public void run() {
    while (true) {
      boolean stopped;
      synchronized (runningLock) {
        if (!isRunning) {
          break;
        } else if (Thread.currentThread().isInterrupted()) {
          isRunning = false;
          break;
        }
        stopped = isStopped;
      }
      try {
        if (requestInterval.isDue() || stopped) {
          sendRequest();
          requestInterval.startNext();
        }
        if (!stopped) {
          threadSleepHandler.sleep();
        } else {
          synchronized (runningLock) {
            isRunning = false;
            executor.shutdown();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  @Override
  public void sendRequest() {
    try {
      Response response = requestSender.send(requestSupplier.get()).get();
      callback.onRequestSuccess(response);
    } catch (InterruptedException | ExecutionException e) {
      callback.onRequestFailed(e);
    }
  }
}
