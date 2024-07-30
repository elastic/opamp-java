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

/**
 * Information that affects how a request is built. Used by implementations of {@link
 * co.elastic.opamp.client.internal.request.visitors.AgentToServerVisitor}.
 */
public final class RequestContext {
  /**
   * This is set to {@link Boolean#TRUE} when the request being built is the last one that the
   * Client will send after it has being stopped. As explained <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#closing-connection">here</a>.
   */
  public final boolean stop;

  /**
   * This is set to {@link Boolean#TRUE} when the Server has requested the Client to report a full
   * state, as explained <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agent-status-compression">here</a>.
   */
  public final boolean disableCompression;

  public RequestContext(boolean stop, boolean disableCompression) {
    this.stop = stop;
    this.disableCompression = disableCompression;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private boolean stop = false;
    private boolean disableCompression = false;

    private Builder() {}

    public Builder stop() {
      stop = true;
      return this;
    }

    public Builder disableCompression() {
      disableCompression = true;
      return this;
    }

    public RequestContext build() {
      return new RequestContext(stop, disableCompression);
    }
  }
}
