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
package co.elastic.opamp.client.internal.request.http.handlers.sleep.impl;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.internal.request.http.handlers.sleep.Sleeper;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FixedThreadSleepHandlerTest {
  @Mock private Sleeper sleeper;

  @Test
  void verifyDefaultBehavior() throws InterruptedException {
    long interval = 123;
    FixedThreadSleepHandler handler = create(interval);

    handler.sleep();

    verify(sleeper).sleep(interval);
  }

  @Test
  void verifyAwakeWhenSleeping() throws InterruptedException {
    FixedThreadSleepHandler handler = sleepAndLock();

    handler.awakeOrIgnoreNextSleep();

    verify(sleeper).awake();
  }

  @Test
  void verifyIgnoreNextSleepingWhenNotSleeping() throws InterruptedException {
    FixedThreadSleepHandler handler = create(123);

    handler.awakeOrIgnoreNextSleep();
    verify(sleeper, never()).awake();

    // Try call sleep:
    handler.sleep();
    verify(sleeper, never()).sleep(anyLong());

    // Try again:
    handler.sleep();
    verify(sleeper).sleep(123);
  }

  private FixedThreadSleepHandler sleepAndLock() throws InterruptedException {
    CountDownLatch testLatch = new CountDownLatch(1);
    FixedThreadSleepHandler handler = new FixedThreadSleepHandler(123, new TestSleeper());
    new Thread(
            () -> {
              try {
                testLatch.countDown();
                handler.sleep();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            })
        .start();

    testLatch.await();

    return handler;
  }

  private FixedThreadSleepHandler create(long intervalMillis) {
    return new FixedThreadSleepHandler(intervalMillis, sleeper);
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
