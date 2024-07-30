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
package co.elastic.opamp.client.internal.request.handlers.sleep;

/** Utility to lock the polling thread between loops for a period of time. */
public interface ThreadSleepHandler {
  /**
   * If the thread is locked, release it right away. If the thread isn't locked, then ignore the
   * next call to {@link #sleep()}.
   */
  void awakeOrIgnoreNextSleep();

  /**
   * Locks the thread for a period of time or until {@link #awakeOrIgnoreNextSleep()} is called.
   *
   * @throws InterruptedException When the thread is interrupted while locked.
   */
  void sleep() throws InterruptedException;
}
