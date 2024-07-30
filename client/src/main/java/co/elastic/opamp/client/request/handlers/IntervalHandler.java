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
package co.elastic.opamp.client.request.handlers;

import co.elastic.opamp.client.internal.request.handlers.FixedIntervalHandler;
import java.time.Duration;

/**
 * Defines when a request is due. AgentToServer HTTP requests are sent periodically when the Agent
 * doesn't have anything to report to the Server, to allow the Server to provide any information to
 * the Agent. More information <a
 * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#plain-http-transport">here</a>.
 *
 * <p>In order to comply with it, the Client runs frequent checks to the {@link IntervalHandler}
 * implementation to ensure when the next request is due.
 */
public interface IntervalHandler {
  static IntervalHandler fixed(Duration interval) {
    return FixedIntervalHandler.of(interval);
  }

  /**
   * Called frequently by the Client to make sure when it can send a message to the Server.
   *
   * @return {@link java.lang.Boolean#TRUE} when a new request is ready to be sent, {@link
   *     java.lang.Boolean#FALSE} otherwise.
   */
  boolean isDue();

  /**
   * Sometimes the Client might need to skip the wait to send a message to the Server immediately,
   * in those cases, the Client should call this method to try and do so right away. The {@link
   * IntervalHandler} implementation might ignore the request.
   *
   * @return {@link java.lang.Boolean#TRUE} if the waiting time has been skipped (which means that
   *     the next call to {@link #isDue()} will return {@link java.lang.Boolean#TRUE}), {@link
   *     java.lang.Boolean#FALSE} if the request to skip the waiting time has been ignored by the
   *     {@link IntervalHandler} implementation.
   */
  boolean fastForward();

  /**
   * Starts the next waiting time period, in which {@link #isDue()} will return {@link
   * java.lang.Boolean#FALSE} until the waiting time is done.
   */
  void startNext();

  /**
   * Called by the Client when it has a recommended duration for the next waiting period. The {@link
   * IntervalHandler} implementation might ignore the suggestion.
   *
   * <p>This method must be called before {@link #startNext()}.
   *
   * @param interval The suggested waiting time for the next wait.
   * @return {@link java.lang.Boolean#TRUE} if the suggestion was accepted, {@link
   *     java.lang.Boolean#FALSE} otherwise.
   */
  boolean suggestNextInterval(Duration interval);

  /** Clears all internal state to its initial value. */
  void reset();
}
