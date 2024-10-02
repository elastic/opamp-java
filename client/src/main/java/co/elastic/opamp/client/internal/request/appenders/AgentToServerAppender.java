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
package co.elastic.opamp.client.internal.request.appenders;

import co.elastic.opamp.client.internal.request.RequestContext;
import opamp.proto.Opamp;

/**
 * AgentToServer request builder appender. Each implementation should match one of the AgentToServer
 * fields and ensure the field is added to a request.
 */
public interface AgentToServerAppender {
  /**
   * Visits a request builder.
   *
   * @param requestContext The context of the request being build. Check {@link RequestContext} for
   *     more details.
   * @param builder The AgentToServer message builder.
   */
  void visit(RequestContext requestContext, Opamp.AgentToServer.Builder builder);
}
