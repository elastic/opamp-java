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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import co.elastic.opamp.client.internal.request.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.handlers.sleep.ThreadSleepHandler;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestDispatcherTest {
  @Mock private Runnable requestRunner;
  @Mock private DualIntervalHandler requestInterval;
  @Mock private ExecutorService executor;
  @Mock private ThreadSleepHandler threadSleepHandler;
  private RequestDispatcher requestDispatcher;

  @BeforeEach
  void setUp() {
    requestDispatcher = new RequestDispatcher(executor, requestInterval, threadSleepHandler);
  }

  @Test
  void verifyStart() {
    requestDispatcher.start(requestRunner);

    InOrder inOrder = inOrder(requestInterval, executor);
    inOrder.verify(requestInterval).startNext();
    inOrder.verify(executor).execute(requestDispatcher);

    // Try starting it again:
    try {
      requestDispatcher.start(requestRunner);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("RequestDispatcher is already running");
    }
  }

  @Test
  void verifyStop() {
    requestDispatcher.start(requestRunner);
    requestDispatcher.stop();

    verify(threadSleepHandler).awakeOrIgnoreNextSleep();

    // Try stopping it again:
    clearInvocations(threadSleepHandler);
    requestDispatcher.stop();
    verifyNoInteractions(threadSleepHandler);
  }

  @Test
  void verifyStop_whenNotStarted() {
    requestDispatcher.stop();

    verifyNoInteractions(threadSleepHandler, requestRunner, requestInterval);
  }

  @Test
  void whenTryingToStartAfterStopHasBeenCalled_throwException() {
    requestDispatcher.start(requestRunner);
    requestDispatcher.stop();
    try {
      requestDispatcher.start(requestRunner);
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("RequestDispatcher has been stopped");
    }
  }

  @Test
  void verifyInitialRetryMode() {
    assertThat(requestDispatcher.isRetryModeEnabled()).isFalse();
  }

  @Test
  void verifyEnablingRetryMode() {
    requestDispatcher.enableRetryMode(null);

    InOrder inOrder = inOrder(requestInterval);
    inOrder.verify(requestInterval).switchToSecondary();
    inOrder.verify(requestInterval).reset();
    assertThat(requestDispatcher.isRetryModeEnabled()).isTrue();
    verifyNoMoreInteractions(requestInterval);
  }

  @Test
  void verifyEnablingRetryMode_withSuggestedInterval() {
    Duration suggestedInterval = Duration.ofSeconds(1);
    requestDispatcher.enableRetryMode(suggestedInterval);

    InOrder inOrder = inOrder(requestInterval);
    inOrder.verify(requestInterval).switchToSecondary();
    inOrder.verify(requestInterval).reset();
    inOrder.verify(requestInterval).suggestNextInterval(suggestedInterval);
    assertThat(requestDispatcher.isRetryModeEnabled()).isTrue();
    verifyNoMoreInteractions(requestInterval);
  }

  @Test
  void verifyEnablingRetryMode_whenItIsAlreadyEnabled() {
    requestDispatcher.enableRetryMode(null);
    clearInvocations(requestInterval);
    assertThat(requestDispatcher.isRetryModeEnabled()).isTrue();

    // Try again:
    requestDispatcher.enableRetryMode(null);
    assertThat(requestDispatcher.isRetryModeEnabled()).isTrue();
    verifyNoInteractions(requestInterval);
  }

  @Test
  void verifyEnablingRetryMode_whenItIsAlreadyEnabled_withSuggestedInterval() {
    requestDispatcher.enableRetryMode(null);
    clearInvocations(requestInterval);
    assertThat(requestDispatcher.isRetryModeEnabled()).isTrue();

    // Try again:
    Duration suggestedInterval = Duration.ofSeconds(1);
    requestDispatcher.enableRetryMode(suggestedInterval);
    assertThat(requestDispatcher.isRetryModeEnabled()).isTrue();
    verify(requestInterval).suggestNextInterval(suggestedInterval);
    verifyNoMoreInteractions(requestInterval);
  }

  @Test
  void verifyDisablingRetryMode() {
    requestDispatcher.enableRetryMode(null);
    clearInvocations(requestInterval);

    requestDispatcher.disableRetryMode();

    InOrder inOrder = inOrder(requestInterval);
    inOrder.verify(requestInterval).switchToMain();
    inOrder.verify(requestInterval).reset();
    assertThat(requestDispatcher.isRetryModeEnabled()).isFalse();
    verifyNoMoreInteractions(requestInterval);
  }

  @Test
  void verifyDisablingRetryMode_whenItIsAlreadyDisabled() {
    requestDispatcher.disableRetryMode();

    assertThat(requestDispatcher.isRetryModeEnabled()).isFalse();
    verifyNoInteractions(requestInterval);
  }

  @Test
  void verifyRunWhenRequestIsDue() throws InterruptedException {
    doReturn(true).when(requestInterval).isDue();

    startAndDispatch(
        dispatchTest -> {
          dispatchTest.thread.interrupt();
          threadSleepHandler.awakeOrIgnoreNextSleep();
          verify(requestRunner).run();
          verify(requestInterval).startNext();
        });
  }

  @Test
  void verifyRunWhenRequestIsNotDue() throws InterruptedException {
    doReturn(false).when(requestInterval).isDue();

    startAndDispatch(
        dispatchTest -> {
          dispatchTest.thread.interrupt();
          threadSleepHandler.awakeOrIgnoreNextSleep();
          verify(requestRunner, never()).run();
          verify(requestInterval, never()).startNext();
        });
  }

  @Test
  void whenStopped_ensureFinalMessageIsSentImmediatelyPriorShutdown() throws InterruptedException {
    doReturn(false).when(requestInterval).isDue();

    startAndDispatch(
        dispatchTest -> {
          dispatchTest.dispatcher.stop();
          InOrder inOrder = inOrder(executor, requestRunner, requestInterval, threadSleepHandler);
          inOrder.verify(threadSleepHandler).awakeOrIgnoreNextSleep();
          inOrder.verify(requestRunner).run();
          inOrder.verify(requestInterval).startNext();
          inOrder.verify(executor).shutdown();
        });
  }

  @Test
  void verifypatchNow_whenIntervalIsCleared() {
    doReturn(true).when(requestInterval).fastForward();

    requestDispatcher.tryDispatchNow();

    verify(threadSleepHandler).awakeOrIgnoreNextSleep();
  }

  @Test
  void verifypatchNow_whenIntervalIsNotCleared() {
    doReturn(false).when(requestInterval).fastForward();

    requestDispatcher.tryDispatchNow();

    verify(threadSleepHandler, never()).awakeOrIgnoreNextSleep();
  }

  private void startAndDispatch(Consumer<DispatchTest> testCase) throws InterruptedException {
    TestThreadSleepHandler testThreadSleepHandler = spy(new TestThreadSleepHandler());
    threadSleepHandler = testThreadSleepHandler;
    CountDownLatch dispatchEndLock = new CountDownLatch(1);
    RequestDispatcher requestDispatcher =
        new RequestDispatcher(executor, requestInterval, threadSleepHandler);
    requestDispatcher.start(requestRunner);
    clearInvocations(requestInterval, executor);
    Thread thread =
        new Thread(
            () -> {
              requestDispatcher.run();
              dispatchEndLock.countDown();
            });
    thread.start();
    testThreadSleepHandler.awaitForDispatcherExecution();
    testCase.accept(new DispatchTest(requestDispatcher, thread));

    if (!dispatchEndLock.await(5, TimeUnit.SECONDS)) {
      fail("The dispatcher did not finish.");
    }
  }

  private static class DispatchTest {
    public final RequestDispatcher dispatcher;
    public final Thread thread;

    private DispatchTest(RequestDispatcher requestDispatcher, Thread thread) {
      this.dispatcher = requestDispatcher;
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
