package co.elastic.opamp.client.internal.request;

import co.elastic.opamp.client.request.handlers.IntervalHandler;
import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestDispatcherTest {
  @Mock private Runnable requestRunner;
  @Mock private IntervalHandler pollingInterval;
  @Mock private IntervalHandler retryInterval;
  private RequestDispatcher requestDispatcher;

  @BeforeEach
  void setUp() {
    requestDispatcher = RequestDispatcher.create(pollingInterval, retryInterval, threadFactory);
  }

  @Test
  void verifyRun() {
    ThreadFactory threadFactory =
        new ThreadFactory() {
          @Override
          public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            return thread;
          }
        };
  }
}
