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
package co.elastic.opamp.client.internal.request.http.handlers;

import co.elastic.opamp.client.request.http.handlers.IntervalHandler;
import java.time.Duration;
import java.util.function.Supplier;

public final class FixedIntervalHandler implements IntervalHandler {
  private final long intervalNanos;
  private final Supplier<Long> nanoTimeSupplier;
  private long startTimeNanos;
  private boolean firstCheck = true;
  private boolean forceDue = false;

  public static FixedIntervalHandler of(Duration interval) {
    return new FixedIntervalHandler(interval.toNanos(), System::nanoTime);
  }

  FixedIntervalHandler(long intervalNanos, Supplier<Long> nanoTimeSupplier) {
    this.intervalNanos = intervalNanos;
    this.nanoTimeSupplier = nanoTimeSupplier;
  }

  @Override
  public boolean isDue() {
    if (firstCheck) {
      firstCheck = false;
      return true;
    }
    if (forceDue) {
      return true;
    }
    return nanoTimeSupplier.get() - startTimeNanos >= intervalNanos;
  }

  @Override
  public boolean fastForward() {
    forceDue = true;
    return true;
  }

  @Override
  public void startNext() {
    forceDue = false;
    startTimeNanos = nanoTimeSupplier.get();
  }

  @Override
  public boolean suggestNextInterval(Duration interval) {
    // Ignored.
    return false;
  }

  @Override
  public void reset() {
    firstCheck = true;
    startNext();
  }
}
