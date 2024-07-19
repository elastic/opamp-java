package co.elastic.opamp.client.internal.request.tools;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FixedSleepScheduleTest {
  @Mock private Sleeper sleeper;

  @Test
  void verifyDefaultBehavior() throws InterruptedException {
    long interval = 123;
    FixedSleepSchedule schedule = create(interval);

    schedule.sleep();

    verify(sleeper).sleep(interval);
  }

  @Test
  void verifyAwakeWhenSleeping() throws InterruptedException {
    FixedSleepSchedule schedule = sleepAndLock();

    schedule.awakeOrIgnoreNextSleep();

    verify(sleeper).awake();
  }

  @Test
  void verifyIgnoreNextSleepingWhenNotSleeping() throws InterruptedException {
    FixedSleepSchedule schedule = create(123);

    schedule.awakeOrIgnoreNextSleep();
    verify(sleeper, never()).awake();

    // Try call sleep:
    schedule.sleep();
    verify(sleeper, never()).sleep(anyLong());

    // Try again:
    schedule.sleep();
    verify(sleeper).sleep(123);
  }

  private FixedSleepSchedule sleepAndLock() throws InterruptedException {
    CountDownLatch testLatch = new CountDownLatch(1);
    FixedSleepSchedule schedule = new FixedSleepSchedule(123, new TestSleeper());
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

  private FixedSleepSchedule create(long intervalMillis) {
    return new FixedSleepSchedule(intervalMillis, sleeper);
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
