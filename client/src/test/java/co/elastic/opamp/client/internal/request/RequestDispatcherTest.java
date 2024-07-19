package co.elastic.opamp.client.internal.request;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.internal.request.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.tools.ThreadSleeper;
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
  private TestThreadSleeper threadSleeper;
  private RequestDispatcher requestDispatcher;

  @BeforeEach
  void setUp() {
    threadSleeper = new TestThreadSleeper();
    requestDispatcher = new RequestDispatcher(executor, requestInterval, threadSleeper);
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
  void verifyRunWhenRequestIsDue() throws InterruptedException {
    doReturn(true).when(requestInterval).isDue();

    startAndDispatch();

    requestDispatcher.stop();
    threadSleeper.releaseDispatcher();
    verify(requestRunner).run();
    verify(requestInterval).startNext();
  }

  @Test
  void verifyRunWhenRequestIsNotDue() throws InterruptedException {
    doReturn(false).when(requestInterval).isDue();

    startAndDispatch();

    requestDispatcher.stop();
    threadSleeper.releaseDispatcher();
    verify(requestRunner, never()).run();
    verify(requestInterval, never()).startNext();
  }

  private void startAndDispatch() throws InterruptedException {
    requestDispatcher.start(requestRunner);
    clearInvocations(requestInterval, executor);
    new Thread(requestDispatcher).start();
    threadSleeper.awaitForDispatcherExecution();
  }

  private static class TestThreadSleeper implements ThreadSleeper {
    private CountDownLatch dispatcherLatch;
    private final CountDownLatch testLatch;

    public TestThreadSleeper() {
      testLatch = new CountDownLatch(1);
    }

    @Override
    public void sleep() throws InterruptedException {
      testLatch.countDown();
      dispatcherLatch = new CountDownLatch(1);
      dispatcherLatch.await();
    }

    public void releaseDispatcher() {
      dispatcherLatch.countDown();
    }

    public void awaitForDispatcherExecution() throws InterruptedException {
      testLatch.await();
    }
  }
}
