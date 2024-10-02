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

import co.elastic.opamp.client.internal.request.appenders.AgentToServerAppenders;
import co.elastic.opamp.client.request.Request;
import java.util.function.Supplier;

public final class RequestProvider implements Supplier<Request> {
  private final AgentToServerAppenders appenders;
  private RequestBuilder requestBuilder;

  public static RequestProvider create(AgentToServerAppenders appenders) {
    RequestProvider requestProvider = new RequestProvider(appenders);
    requestProvider.resetBuilder();
    return requestProvider;
  }

  public void stop() {
    requestBuilder.stop();
  }

  public void disableCompression() {
    requestBuilder.disableCompression();
  }

  @Override
  public Request get() {
    Request request = requestBuilder.build();
    resetBuilder();
    return request;
  }

  private RequestProvider(AgentToServerAppenders appenders) {
    this.appenders = appenders;
  }

  private void resetBuilder() {
    requestBuilder = RequestBuilder.create(appenders);
  }
}
