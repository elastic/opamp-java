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
package co.elastic.opamp.client.internal.request.handlers;

import co.elastic.opamp.client.request.handlers.IntervalHandler;
import java.time.Duration;

public final class DualIntervalHandler implements IntervalHandler {
  private final IntervalHandler main;
  private final IntervalHandler secondary;
  private IntervalHandler current;

  public static DualIntervalHandler of(IntervalHandler main, IntervalHandler secondary) {
    DualIntervalHandler dualIntervalHandler = new DualIntervalHandler(main, secondary);
    dualIntervalHandler.switchToMain();
    return dualIntervalHandler;
  }

  private DualIntervalHandler(IntervalHandler main, IntervalHandler secondary) {
    this.main = main;
    this.secondary = secondary;
  }

  @Override
  public synchronized boolean isDue() {
    return current.isDue();
  }

  @Override
  public synchronized boolean fastForward() {
    return current.fastForward();
  }

  @Override
  public synchronized void startNext() {
    current.startNext();
  }

  @Override
  public synchronized boolean suggestNextInterval(Duration interval) {
    return current.suggestNextInterval(interval);
  }

  @Override
  public synchronized void reset() {
    current.reset();
  }

  public synchronized void switchToMain() {
    current = main;
  }

  public synchronized void switchToSecondary() {
    current = secondary;
  }
}
