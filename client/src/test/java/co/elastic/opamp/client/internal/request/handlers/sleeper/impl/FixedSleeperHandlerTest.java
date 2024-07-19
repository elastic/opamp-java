package co.elastic.opamp.client.internal.request.handlers.sleeper.impl;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.internal.request.handlers.sleeper.Sleeper;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FixedSleeperHandlerTest {
  @Mock private Sleeper sleeper;

  @Test
  void verifyDefaultBehavior() throws InterruptedException {
    long interval = 123;
    FixedSleeperHandler schedule = create(interval);

    schedule.sleep();

    verify(sleeper).sleep(interval);
  }

  @Test
  void verifyAwakeWhenSleeping() throws InterruptedException {
    FixedSleeperHandler schedule = sleepAndLock();

    schedule.awakeOrIgnoreNextSleep();

    verify(sleeper).awake();
  }

  @Test
  void verifyIgnoreNextSleepingWhenNotSleeping() throws InterruptedException {
    FixedSleeperHandler schedule = create(123);

    schedule.awakeOrIgnoreNextSleep();
    verify(sleeper, never()).awake();

    // Try call sleep:
    schedule.sleep();
    verify(sleeper, never()).sleep(anyLong());

    // Try again:
    schedule.sleep();
    verify(sleeper).sleep(123);
  }

  private FixedSleeperHandler sleepAndLock() throws InterruptedException {
    CountDownLatch testLatch = new CountDownLatch(1);
    FixedSleeperHandler schedule = new FixedSleeperHandler(123, new TestSleeper());
    new Thread(
            () -> {
              try {
                testLatch.countDown();
                schedule.sleep();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            })
        .start();

    testLatch.await();

    return schedule;
  }

  private FixedSleeperHandler create(long intervalMillis) {
    return new FixedSleeperHandler(intervalMillis, sleeper);
  }

  private class TestSleeper implements Sleeper {
    private CountDownLatch sleepLatch;

    @Override
    public void sleep(long millis) throws InterruptedException {
      this.sleepLatch = new CountDownLatch(1);
      sleeper.sleep(millis);
      sleepLatch.await();
    }

    @Override
    public void awake() {
      sleeper.awake();
      sleepLatch.countDown();
    }
  }
}
