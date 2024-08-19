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
package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.connectivity.websocket.WebSocket;
import co.elastic.opamp.client.connectivity.websocket.WebSocketListener;
import co.elastic.opamp.client.internal.request.RequestBuilder;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.state.OpampClientState;
import com.google.protobuf.CodedInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import opamp.proto.Opamp;

public class WebSocketOpampClient implements OpampClient, WebSocketListener {
  private final WebSocket webSocket;
  private final RequestBuilder requestBuilder;
  private final OpampClientState state;
  private Callback callback;
  private AtomicBoolean sent = new AtomicBoolean(false);

  public static WebSocketOpampClient create(
      WebSocket webSocket, OpampClientVisitors visitors, OpampClientState state) {
    RequestBuilder requestBuilder = RequestBuilder.create(visitors);
    return new WebSocketOpampClient(webSocket, requestBuilder, state);
  }

  public WebSocketOpampClient(
      WebSocket webSocket, RequestBuilder requestBuilder, OpampClientState state) {
    this.webSocket = webSocket;
    this.requestBuilder = requestBuilder;
    this.state = state;
  }

  @Override
  public void start(Callback callback) {
    this.callback = callback;
    webSocket.start(this);
  }

  @Override
  public void stop() {
    webSocket.stop();
  }

  @Override
  public void setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus) {}

  @Override
  public void setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig) {}

  @Override
  public void onOpened(WebSocket webSocket) {
    callback.onConnect(this);
    if (sent.compareAndSet(false, true)) {
      webSocket.send(requestBuilder.buildAndReset());
    }
  }

  @Override
  public void onMessage(WebSocket webSocket, byte[] data) {
    try {
      CodedInputStream codedInputStream = CodedInputStream.newInstance(data);
      long header = codedInputStream.readRawVarint64();
      int totalBytesRead = codedInputStream.getTotalBytesRead();
      int payloadSize = data.length - totalBytesRead;
      byte[] payload = new byte[payloadSize];
      System.arraycopy(data, totalBytesRead, payload, 0, payloadSize);
      Opamp.ServerToAgent serverToAgent = Opamp.ServerToAgent.parseFrom(payload);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onClosed(WebSocket webSocket) {}

  @Override
  public void onFailure(WebSocket webSocket, Throwable t) {
    callback.onConnectFailed(this, t);
  }
}
