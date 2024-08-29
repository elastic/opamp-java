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
package co.elastic.opamp.client.internal.request.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.internal.periodictask.PeriodicTaskExecutor;
import co.elastic.opamp.client.request.HttpSender;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestService;
import co.elastic.opamp.client.request.delay.PeriodicDelay;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpRequestServiceTest {
  @Mock private HttpSender requestSender;
  @Mock private PeriodicDelay periodicRequestDelay;
  @Mock private PeriodicDelay periodicRetryDelay;
  @Mock private PeriodicTaskExecutor executor;
  @Mock private RequestService.Callback callback;
  @Mock private Supplier<Request> requestSupplier;
  @Mock private Request request;
  private static final int REQUEST_SIZE = 100;
  private HttpRequestService httpRequestService;

  @BeforeEach
  void setUp() {
    httpRequestService =
        new HttpRequestService(requestSender, executor, periodicRequestDelay, periodicRetryDelay);
  }

  @Test
  void verifyStart() {
    httpRequestService.start(callback, requestSupplier);

    InOrder inOrder = inOrder(periodicRequestDelay, executor);
    inOrder.verify(executor).start(httpRequestService);

    // Try starting it again:
    try {
      httpRequestService.start(callback, requestSupplier);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("RequestDispatcher is already running");
    }
  }

  @Test
  void verifyStop() {
    httpRequestService.start(callback, requestSupplier);
    httpRequestService.stop();

    verify(executor).stop();

    // Try stopping it again:
    clearInvocations(executor);
    httpRequestService.stop();
    verifyNoInteractions(executor);
  }

  @Test
  void verifyStop_whenNotStarted() {
    httpRequestService.stop();

    verifyNoInteractions(executor, requestSender, periodicRequestDelay);
  }

  @Test
  void whenTryingToStartAfterStopHasBeenCalled_throwException() {
    httpRequestService.start(callback, requestSupplier);
    httpRequestService.stop();
    try {
      httpRequestService.start(callback, requestSupplier);
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("RequestDispatcher has been stopped");
    }
  }

  private void prepareRequest() {
    Opamp.AgentToServer agentToServer = mock(Opamp.AgentToServer.class);
    doReturn(REQUEST_SIZE).when(agentToServer).getSerializedSize();
    doReturn(agentToServer).when(request).getAgentToServer();
    doReturn(request).when(requestSupplier).get();
    doReturn(CompletableFuture.completedFuture(mock(HttpSender.Response.class)))
        .when(requestSender)
        .send(any(), anyInt());
  }
}
