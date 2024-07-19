package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.internal.request.handlers.DualIntervalHandler;
import co.elastic.opamp.client.internal.request.handlers.sleep.ThreadSleepHandler;
import co.elastic.opamp.client.internal.request.handlers.sleep.impl.FixedThreadSleepHandler;
import co.elastic.opamp.client.request.handlers.IntervalHandler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RequestDispatcher implements Runnable {
  private final ExecutorService executor;
  private final DualIntervalHandler requestInterval;
  private final ThreadSleepHandler threadSleepHandler;
  private final Object runningLock = new Object();
  private boolean retryModeEnabled = false;
  private boolean isRunning = false;
  private Runnable requestRunner;

  RequestDispatcher(
      ExecutorService executor,
      DualIntervalHandler requestInterval,
      ThreadSleepHandler threadSleepHandler) {
    this.executor = executor;
    this.requestInterval = requestInterval;
    this.threadSleepHandler = threadSleepHandler;
  }

  public static RequestDispatcher create(
      IntervalHandler pollingInterval, IntervalHandler retryInterval) {
    return new RequestDispatcher(
        Executors.newSingleThreadExecutor(),
        DualIntervalHandler.of(pollingInterval, retryInterval),
        FixedThreadSleepHandler.of(Duration.ofSeconds(1)));
  }

  public void start(Runnable requestRunner) {
    synchronized (runningLock) {
      if (isRunning) {
        throw new IllegalStateException("RequestDispatcher is already running");
      }
      this.requestRunner = requestRunner;
      requestInterval.startNext();
      executor.execute(this);
      isRunning = true;
    }
  }

  public void stop() {
    synchronized (runningLock) {
      if (!isRunning) {
        return;
      }
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
    if (requestInterval.fastForward()) {
      threadSleepHandler.awakeOrIgnoreNextSleep();
    }
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
        threadSleepHandler.sleep();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
