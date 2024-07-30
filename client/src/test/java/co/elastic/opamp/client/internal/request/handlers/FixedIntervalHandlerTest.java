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
package co.elastic.opamp.client.internal.request.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixedIntervalHandlerTest {
  private static final long INTERVAL_NANOS = 1000;
  private static final long INITIAL_NANO_TIME = 500;
  private Supplier<Long> nanoTimeSupplier;
  private FixedIntervalHandler handler;

  @BeforeEach
  void setUp() {
    nanoTimeSupplier = mock();
    doReturn(INITIAL_NANO_TIME).when(nanoTimeSupplier).get();
    handler = new FixedIntervalHandler(INTERVAL_NANOS, nanoTimeSupplier);
  }

  @Test
  void verifyFirstCheckSucceeds() {
    handler.startNext();
    assertThat(handler.isDue()).isTrue();
  }

  @Test
  void verifyReset() {
    handler.startNext();
    assertThat(handler.isDue()).isTrue();
    assertThat(handler.isDue()).isFalse();

    handler.reset();

    assertThat(handler.isDue()).isTrue();
  }

  @Test
  void verifyNextCheckWaitsForInterval() {
    handler.startNext();
    // First time:
    assertThat(handler.isDue()).isTrue();
    // Next time:
    assertThat(handler.isDue()).isFalse();

    // Wait for less than the interval:
    doReturn(INITIAL_NANO_TIME + 1).when(nanoTimeSupplier).get();
    assertThat(handler.isDue()).isFalse();

    // Wait for interval:
    doReturn(INITIAL_NANO_TIME + INTERVAL_NANOS).when(nanoTimeSupplier).get();
    assertThat(handler.isDue()).isTrue();

    // Wait for more than the interval:
    doReturn(INITIAL_NANO_TIME + INTERVAL_NANOS + 1).when(nanoTimeSupplier).get();
    assertThat(handler.isDue()).isTrue();
  }

  @Test
  void verifyFastForwardDoesNotWaitForInterval() {
    handler.startNext();
    // First time:
    assertThat(handler.isDue()).isTrue();
    // Next time:
    assertThat(handler.isDue()).isFalse();

    handler.fastForward();

    assertThat(handler.isDue()).isTrue();
    // Is true all the time (until the next interval starts).
    assertThat(handler.isDue()).isTrue();
    handler.startNext();
    assertThat(handler.isDue()).isFalse();
  }

  @Test
  void verifySuggestionsAreIgnored() {
    assertThat(handler.suggestNextInterval(Duration.ofSeconds(1))).isFalse();
  }
}
