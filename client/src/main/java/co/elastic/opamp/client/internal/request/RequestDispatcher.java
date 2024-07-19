package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.internal.request.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.tools.FixedThreadSleeper;
import co.elastic.opamp.client.internal.request.tools.ThreadSleeper;
import co.elastic.opamp.client.request.handlers.IntervalHandler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RequestDispatcher implements Runnable {
  private final ExecutorService executor;
  private final DualIntervalHandler requestInterval;
  private final ThreadSleeper threadSleeper;
  private final Object runningLock = new Object();
  private boolean retryModeEnabled = false;
  private boolean isRunning = false;
  private Runnable requestRunner;

  RequestDispatcher(
      ExecutorService executor, DualIntervalHandler requestInterval, ThreadSleeper threadSleeper) {
    this.executor = executor;
    this.requestInterval = requestInterval;
    this.threadSleeper = threadSleeper;
  }

  public static RequestDispatcher create(
      IntervalHandler pollingInterval, IntervalHandler retryInterval) {
    return new RequestDispatcher(
        Executors.newSingleThreadExecutor(),
        DualIntervalHandler.of(pollingInterval, retryInterval),
        FixedThreadSleeper.of(Duration.ofSeconds(1)));
  }

  public void start(Runnable requestRunner) {
    synchronized (runningLock) {
      this.requestRunner = requestRunner;
      requestInterval.startNext();
      executor.execute(this);
      isRunning = true;
    }
  }

  public void stop() {
    synchronized (runningLock) {
      executor.shutdown();
      isRunning = false;
    }
  }

  public boolean isRetryModeEnabled() {
    return retryModeEnabled;
  }

  public void enableRetryMode(Duration suggestedInterval) {
    if (!retryModeEnabled) {
      retryModeEnabled = true;
      requestInterval.switchToSecondary();
      requestInterval.reset();
    }
    if (suggestedInterval != null) {
      requestInterval.suggestNextInterval(suggestedInterval);
    }
  }

  public void disableRetryMode() {
    if (retryModeEnabled) {
      retryModeEnabled = false;
      requestInterval.switchToMain();
      requestInterval.reset();
    }
  }

  public void tryDispatchNow() {
    requestInterval.fastForward();
  }

  @Override
  public void run() {
    while (true) {
      synchronized (runningLock) {
        if (!isRunning) {
          break;
        }
      }
      try {
        if (requestInterval.isDue()) {
          requestRunner.run();
          requestInterval.startNext();
        }
        threadSleeper.sleep();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
