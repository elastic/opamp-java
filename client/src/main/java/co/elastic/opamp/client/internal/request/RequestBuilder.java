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
import opamp.proto.Opamp;

public final class RequestBuilder {
  private final AgentToServerAppenders appenders;
  private boolean stop = false;
  private boolean disableCompression = false;

  public static RequestBuilder create(AgentToServerAppenders appenders) {
    return new RequestBuilder(appenders);
  }

  RequestBuilder(AgentToServerAppenders appenders) {
    this.appenders = appenders;
  }

  public RequestBuilder stop() {
    this.stop = true;
    return this;
  }

  public RequestBuilder disableCompression() {
    this.disableCompression = true;
    return this;
  }

  public Request build() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    RequestContext context =
        RequestContext.builder().setStop(stop).setDisableCompression(disableCompression).build();
    appenders.asList().forEach(appender -> appender.visit(context, builder));
    return Request.create(builder.build());
  }
}
