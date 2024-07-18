package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.internal.request.handlers.DualIntervalHandler;
import co.elastic.opamp.client.request.handlers.IntervalHandler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RequestDispatcher implements Runnable {
  private final ExecutorService executor;
  private final DualIntervalHandler requestInterval;
  private final Object runningLock = new Object();
  private boolean retryModeEnabled = false;
  private boolean isRunning = false;
  private Runnable requestRunner;

  RequestDispatcher(ExecutorService executor, DualIntervalHandler requestInterval) {
    this.executor = executor;
    this.requestInterval = requestInterval;
  }

  public static RequestDispatcher create(
      IntervalHandler pollingInterval, IntervalHandler retryInterval) {
    return new RequestDispatcher(
        Executors.newSingleThreadExecutor(),
        DualIntervalHandler.of(pollingInterval, retryInterval));
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
      try {
        if (requestInterval.isDue()) {
          requestRunner.run();
          requestInterval.startNext();
        }
        synchronized (runningLock) {
          if (!isRunning) {
            break;
          }
        }
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
