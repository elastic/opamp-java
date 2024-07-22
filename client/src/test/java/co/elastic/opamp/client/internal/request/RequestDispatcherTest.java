package co.elastic.opamp.client.internal.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import co.elastic.opamp.client.internal.request.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.handlers.sleep.ThreadSleepHandler;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
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
    } catch (IllegalStateException ignored) {
    }
  }

  @Test
  void verifyStop() {
    requestDispatcher.start(requestRunner);
    requestDispatcher.stop();

    verify(executor).shutdown();

    // Try stopping it again:
    clearInvocations(executor);
    requestDispatcher.stop();
    verifyNoInteractions(executor);
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
    TestThreadSleepHandler threadSleepHandler = new TestThreadSleepHandler();

    Thread dispatchingThread = startAndDispatch(threadSleepHandler);

    dispatchingThread.interrupt();
    threadSleepHandler.awakeOrIgnoreNextSleep();
    verify(requestRunner).run();
    verify(requestInterval).startNext();
  }

  @Test
  void verifyRunWhenRequestIsNotDue() throws InterruptedException {
    doReturn(false).when(requestInterval).isDue();
    TestThreadSleepHandler threadSleepHandler = new TestThreadSleepHandler();

    Thread dispatchingThread = startAndDispatch(threadSleepHandler);

    dispatchingThread.interrupt();
    threadSleepHandler.awakeOrIgnoreNextSleep();
    verify(requestRunner, never()).run();
    verify(requestInterval, never()).startNext();
  }

  @Test
  void verifyTryDispatchNow_whenIntervalIsCleared() {
    doReturn(true).when(requestInterval).fastForward();

    requestDispatcher.tryDispatchNow();

    verify(threadSleepHandler).awakeOrIgnoreNextSleep();
  }

  @Test
  void verifyTryDispatchNow_whenIntervalIsNotCleared() {
    doReturn(false).when(requestInterval).fastForward();

    requestDispatcher.tryDispatchNow();

    verify(threadSleepHandler, never()).awakeOrIgnoreNextSleep();
  }

  private Thread startAndDispatch(TestThreadSleepHandler threadSleepHandler)
      throws InterruptedException {
    RequestDispatcher requestDispatcher =
        new RequestDispatcher(executor, requestInterval, threadSleepHandler);
    requestDispatcher.start(requestRunner);
    clearInvocations(requestInterval, executor);
    Thread thread = new Thread(requestDispatcher);
    thread.start();
    threadSleepHandler.awaitForDispatcherExecution();
    return thread;
  }

  private static class TestThreadSleepHandler implements ThreadSleepHandler {
    private CountDownLatch dispatcherLatch;
    private final CountDownLatch testLatch;

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
