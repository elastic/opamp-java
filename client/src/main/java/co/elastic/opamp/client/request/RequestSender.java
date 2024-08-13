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
package co.elastic.opamp.client.request;

import opamp.proto.Opamp;

/** Interface for any HTTP request sender implementation. */
public interface RequestSender {

  /**
   * Sends a request synchronously.
   *
   * @param request The request to be sent.
   * @return {@link Response.Success} for responses with a "ServerToAgent" body, {@link
   *     Response.Error} for when there's either an HTTP error code or the connection couldn't be
   *     established due to IO errors. For HTTP errors, the {@link java.lang.Throwable} provided in
   *     {@link Response.Error} should be of type {@link
   *     co.elastic.opamp.client.request.http.HttpErrorException}.
   */
  Response send(Request request);

  interface Response {
    static Response success(Opamp.ServerToAgent data) {
      return new Response.Success(data);
    }

    static Response error(Throwable throwable) {
      return new Error(throwable);
    }

    final class Success implements Response {
      public final Opamp.ServerToAgent data;

      private Success(Opamp.ServerToAgent data) {
        this.data = data;
      }
    }

    final class Error implements Response {
      public final Throwable throwable;

      private Error(Throwable throwable) {
        this.throwable = throwable;
      }
    }
  }
}
