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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.internal.request.http.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.http.handlers.sleep.ThreadSleepHandler;
import co.elastic.opamp.client.request.HttpSender;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
  @Mock private DualIntervalHandler requestInterval;
  @Mock private ExecutorService executor;
  @Mock private ThreadSleepHandler threadSleepHandler;
  @Mock private RequestService.Callback callback;
  @Mock private Supplier<Request> requestSupplier;
  @Mock private Request request;
  private static final int REQUEST_SIZE = 100;
  private HttpRequestService httpRequestService;

  @BeforeEach
  void setUp() {
    httpRequestService =
        new HttpRequestService(requestSender, executor, requestInterval, threadSleepHandler);
  }

  @Test
  void verifyStart() {
    httpRequestService.start(callback, requestSupplier);

    InOrder inOrder = inOrder(requestInterval, executor);
    inOrder.verify(requestInterval).startNext();
    inOrder.verify(executor).execute(httpRequestService);

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

    verify(threadSleepHandler).awakeOrIgnoreNextSleep();

    // Try stopping it again:
    clearInvocations(threadSleepHandler);
    httpRequestService.stop();
    verifyNoInteractions(threadSleepHandler);
  }

  @Test
  void verifyStop_whenNotStarted() {
    httpRequestService.stop();

    verifyNoInteractions(threadSleepHandler, requestSender, requestInterval);
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

  @Test
  void verifySendingRequestWhenIsDue() throws InterruptedException {
    prepareRequest();
    doReturn(true).when(requestInterval).isDue();

    startAndSendRequest(
        dispatchTest -> {
          dispatchTest.thread.interrupt();
          threadSleepHandler.awakeOrIgnoreNextSleep();
          verify(requestSender).send(any(), eq(REQUEST_SIZE));
          verify(requestInterval).startNext();
        });
  }

  @Test
  void verifyNotSendingRequestWhenIsNotDue() throws InterruptedException {
    doReturn(false).when(requestInterval).isDue();

    startAndSendRequest(
        dispatchTest -> {
          dispatchTest.thread.interrupt();
          threadSleepHandler.awakeOrIgnoreNextSleep();
          verify(requestSender, never()).send(any(), anyInt());
          verify(requestInterval, never()).startNext();
        });
  }

  @Test
  void whenStopped_ensureFinalMessageIsSentImmediatelyPriorShutdown() throws InterruptedException {
    prepareRequest();
    doReturn(false).when(requestInterval).isDue();

    startAndSendRequest(
        dispatchTest -> {
          dispatchTest.dispatcher.stop();
          InOrder inOrder = inOrder(executor, requestSender, requestInterval, threadSleepHandler);
          inOrder.verify(threadSleepHandler).awakeOrIgnoreNextSleep();
          inOrder.verify(requestSender).send(any(), eq(REQUEST_SIZE));
          inOrder.verify(requestInterval).startNext();
          inOrder.verify(executor).shutdown();
        });
  }

  @Test
  void verifySendRequest_whenIntervalIsCleared() {
    doReturn(true).when(requestInterval).fastForward();

    httpRequestService.sendRequest();

    verify(threadSleepHandler).awakeOrIgnoreNextSleep();
  }

  @Test
  void verifySendRequest_whenIntervalIsNotCleared() {
    doReturn(false).when(requestInterval).fastForward();

    httpRequestService.sendRequest();

    verify(threadSleepHandler, never()).awakeOrIgnoreNextSleep();
  }

  //  @Test
  //  void verifyInitialRetryMode() {
  //    assertThat(httpRequestService.isRetryModeEnabled()).isFalse();
  //  }
  //
  //  @Test
  //  void verifyEnablingRetryMode() {
  //    httpRequestService.enableRetryMode(null);
  //
  //    InOrder inOrder = inOrder(requestInterval);
  //    inOrder.verify(requestInterval).switchToSecondary();
  //    inOrder.verify(requestInterval).reset();
  //    assertThat(httpRequestService.isRetryModeEnabled()).isTrue();
  //    verifyNoMoreInteractions(requestInterval);
  //  }
  //
  //  @Test
  //  void verifyEnablingRetryMode_withSuggestedInterval() {
  //    Duration suggestedInterval = Duration.ofSeconds(1);
  //    httpRequestService.enableRetryMode(suggestedInterval);
  //
  //    InOrder inOrder = inOrder(requestInterval);
  //    inOrder.verify(requestInterval).switchToSecondary();
  //    inOrder.verify(requestInterval).reset();
  //    inOrder.verify(requestInterval).suggestNextInterval(suggestedInterval);
  //    assertThat(httpRequestService.isRetryModeEnabled()).isTrue();
  //    verifyNoMoreInteractions(requestInterval);
  //  }
  //
  //  @Test
  //  void verifyEnablingRetryMode_whenItIsAlreadyEnabled() {
  //    httpRequestService.enableRetryMode(null);
  //    clearInvocations(requestInterval);
  //    assertThat(httpRequestService.isRetryModeEnabled()).isTrue();
  //
  //    // Try again:
  //    httpRequestService.enableRetryMode(null);
  //    assertThat(httpRequestService.isRetryModeEnabled()).isTrue();
  //    verifyNoInteractions(requestInterval);
  //  }
  //
  //  @Test
  //  void verifyEnablingRetryMode_whenItIsAlreadyEnabled_withSuggestedInterval() {
  //    httpRequestService.enableRetryMode(null);
  //    clearInvocations(requestInterval);
  //    assertThat(httpRequestService.isRetryModeEnabled()).isTrue();
  //
  //    // Try again:
  //    Duration suggestedInterval = Duration.ofSeconds(1);
  //    httpRequestService.enableRetryMode(suggestedInterval);
  //    assertThat(httpRequestService.isRetryModeEnabled()).isTrue();
  //    verify(requestInterval).suggestNextInterval(suggestedInterval);
  //    verifyNoMoreInteractions(requestInterval);
  //  }
  //
  //  @Test
  //  void verifyDisablingRetryMode() {
  //    httpRequestService.enableRetryMode(null);
  //    clearInvocations(requestInterval);
  //
  //    httpRequestService.disableRetryMode();
  //
  //    InOrder inOrder = inOrder(requestInterval);
  //    inOrder.verify(requestInterval).switchToMain();
  //    inOrder.verify(requestInterval).reset();
  //    assertThat(httpRequestService.isRetryModeEnabled()).isFalse();
  //    verifyNoMoreInteractions(requestInterval);
  //  }
  //
  //  @Test
  //  void verifyDisablingRetryMode_whenItIsAlreadyDisabled() {
  //    httpRequestService.disableRetryMode();
  //
  //    assertThat(httpRequestService.isRetryModeEnabled()).isFalse();
  //    verifyNoInteractions(requestInterval);
  //  }
  //
  private void startAndSendRequest(Consumer<DispatchTest> testCase) throws InterruptedException {
    TestThreadSleepHandler testThreadSleepHandler = spy(new TestThreadSleepHandler());
    threadSleepHandler = testThreadSleepHandler;
    CountDownLatch dispatchEndLock = new CountDownLatch(1);
    HttpRequestService httpRequestDispatcher =
        new HttpRequestService(requestSender, executor, requestInterval, threadSleepHandler);
    httpRequestDispatcher.start(callback, requestSupplier);
    clearInvocations(requestInterval, executor);
    Thread thread =
        new Thread(
            () -> {
              httpRequestDispatcher.run();
              dispatchEndLock.countDown();
            });
    thread.start();
    testThreadSleepHandler.awaitForDispatcherExecution();
    testCase.accept(new DispatchTest(httpRequestDispatcher, thread));

    if (!dispatchEndLock.await(5, TimeUnit.SECONDS)) {
      fail("The dispatcher did not finish.");
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

  private static class DispatchTest {
    public final HttpRequestService dispatcher;
    public final Thread thread;

    private DispatchTest(HttpRequestService httpRequestDispatcher, Thread thread) {
      this.dispatcher = httpRequestDispatcher;
      this.thread = thread;
    }
  }

  private static class TestThreadSleepHandler implements ThreadSleepHandler {
    private final CountDownLatch testLatch;
    private CountDownLatch dispatcherLatch;

    public TestThreadSleepHandler() {
      testLatch = new CountDownLatch(1);
    }

    @Override
    public void awakeOrIgnoreNextSleep() {
      dispatcherLatch.countDown();
    }

    @Override
    public void sleep() throws InterruptedException {
      testLatch.countDown();
      dispatcherLatch = new CountDownLatch(1);
      dispatcherLatch.await();
    }

    public void awaitForDispatcherExecution() throws InterruptedException {
      testLatch.await();
    }
  }
}
