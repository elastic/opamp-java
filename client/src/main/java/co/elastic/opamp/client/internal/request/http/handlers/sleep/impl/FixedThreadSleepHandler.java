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
package co.elastic.opamp.client.internal.request.http.handlers.sleep.impl;

import co.elastic.opamp.client.internal.request.http.handlers.sleep.Sleeper;
import co.elastic.opamp.client.internal.request.http.handlers.sleep.ThreadSleepHandler;
import java.time.Duration;

public final class FixedThreadSleepHandler implements ThreadSleepHandler {
  private final long intervalMillis;
  private final Sleeper sleeper;
  private final Object sleepingLock = new Object();
  private boolean isSleeping = false;
  private boolean ignoreNextSleep = false;

  public static FixedThreadSleepHandler of(Duration interval) {
    return new FixedThreadSleepHandler(interval.toMillis(), Sleeper.create());
  }

  FixedThreadSleepHandler(long intervalMillis, Sleeper sleeper) {
    this.intervalMillis = intervalMillis;
    this.sleeper = sleeper;
  }

  @Override
  public void awakeOrIgnoreNextSleep() {
    synchronized (sleepingLock) {
      if (isSleeping) {
        sleeper.awake();
      } else {
        ignoreNextSleep = true;
      }
    }
  }

  @Override
  public void sleep() throws InterruptedException {
    synchronized (sleepingLock) {
      if (ignoreNextSleep) {
        ignoreNextSleep = false;
        return;
      }
      isSleeping = true;
    }
    sleeper.sleep(intervalMillis);
    synchronized (sleepingLock) {
      isSleeping = false;
    }
  }
}
