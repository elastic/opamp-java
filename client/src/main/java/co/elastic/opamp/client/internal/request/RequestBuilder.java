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

import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.request.Request;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public final class RequestBuilder {
  private final OpampClientVisitors visitors;
  private final Supplier<RequestContext.Builder> contextBuilderSupplier;
  private RequestContext.Builder contextBuilder;

  public static RequestBuilder create(OpampClientVisitors visitors) {
    return new RequestBuilder(visitors, RequestContext::newBuilder);
  }

  RequestBuilder(
      OpampClientVisitors visitors, Supplier<RequestContext.Builder> contextBuilderSupplier) {
    this.visitors = visitors;
    this.contextBuilderSupplier = contextBuilderSupplier;
    contextBuilder = contextBuilderSupplier.get();
  }

  public Request buildAndReset() {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    RequestContext requestContext = contextBuilder.build();
    visitors.asList().forEach(visitor -> visitor.visit(requestContext, builder));
    contextBuilder = contextBuilderSupplier.get();
    return Request.create(builder.build());
  }

  public void stop() {
    contextBuilder.stop();
  }

  public void disableCompression() {
    contextBuilder.disableCompression();
  }
}
