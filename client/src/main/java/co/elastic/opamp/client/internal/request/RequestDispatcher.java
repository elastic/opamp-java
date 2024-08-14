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
package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.connectivity.http.handlers.IntervalHandler;
import co.elastic.opamp.client.internal.request.http.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.http.handlers.sleep.ThreadSleepHandler;
import co.elastic.opamp.client.internal.request.http.handlers.sleep.impl.FixedThreadSleepHandler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RequestDispatcher implements Runnable {
  private final ExecutorService executor;
  private final DualIntervalHandler requestInterval;
  private final ThreadSleepHandler threadSleepHandler;
  private final Object runningLock = new Object();
  private boolean retryModeEnabled = false;
  private boolean isRunning = false;
  private boolean isStopped = false;
  private Runnable requestRunner;

  RequestDispatcher(
      ExecutorService executor,
      DualIntervalHandler requestInterval,
      ThreadSleepHandler threadSleepHandler) {
    this.executor = executor;
    this.requestInterval = requestInterval;
    this.threadSleepHandler = threadSleepHandler;
  }

  public static RequestDispatcher create(
      IntervalHandler pollingInterval, IntervalHandler retryInterval) {
    return new RequestDispatcher(
        Executors.newSingleThreadExecutor(),
        DualIntervalHandler.of(pollingInterval, retryInterval),
        FixedThreadSleepHandler.of(Duration.ofSeconds(1)));
  }

  public void start(Runnable requestRunner) {
    synchronized (runningLock) {
      if (isStopped) {
        throw new IllegalStateException("RequestDispatcher has been stopped");
      }
      if (isRunning) {
        throw new IllegalStateException("RequestDispatcher is already running");
      }
      this.requestRunner = requestRunner;
      requestInterval.startNext();
      executor.execute(this);
      isRunning = true;
    }
  }

  public void stop() {
    synchronized (runningLock) {
      if (!isRunning || isStopped) {
        return;
      }
      isStopped = true;
      threadSleepHandler.awakeOrIgnoreNextSleep();
    }
  }

  public boolean isRetryModeEnabled() {
    return retryModeEnabled;
  }

  public void enableRetryMode(Duration suggestedInterval) {
    if (!retryModeEnabled) {
      retryModeEnabled = true;
      requestInterval.switchToSecondary();
      requestInterval.reset();
    }
    if (suggestedInterval != null) {
      requestInterval.suggestNextInterval(suggestedInterval);
    }
  }

  public void disableRetryMode() {
    if (retryModeEnabled) {
      retryModeEnabled = false;
      requestInterval.switchToMain();
      requestInterval.reset();
    }
  }

  public void tryDispatchNow() {
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
          requestRunner.run();
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
}
