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
