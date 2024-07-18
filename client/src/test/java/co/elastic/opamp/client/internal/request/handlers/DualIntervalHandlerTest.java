package co.elastic.opamp.client.internal.request.handlers;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.request.handlers.IntervalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DualIntervalHandlerTest {
  @Mock private IntervalHandler main;
  @Mock private IntervalHandler secondary;
  private DualIntervalHandler dualIntervalHandler;

  @BeforeEach
  void setUp() {
    dualIntervalHandler = DualIntervalHandler.of(main, secondary);
  }

  @Test
  void verifyDefaultIntervalHandler() {
    dualIntervalHandler.fastForward();
    dualIntervalHandler.startNext();
    dualIntervalHandler.isDue();
    dualIntervalHandler.reset();

    InOrder inOrder = inOrder(main);
    inOrder.verify(main).fastForward();
    inOrder.verify(main).startNext();
    inOrder.verify(main).isDue();
    inOrder.verify(main).reset();
    verifyNoInteractions(secondary);
  }

  @Test
  void verifySwitchToSecondary() {
    dualIntervalHandler.switchToSecondary();
    dualIntervalHandler.fastForward();
    dualIntervalHandler.startNext();
    dualIntervalHandler.isDue();
    dualIntervalHandler.reset();

    InOrder inOrder = inOrder(secondary);
    inOrder.verify(secondary).fastForward();
    inOrder.verify(secondary).startNext();
    inOrder.verify(secondary).isDue();
    inOrder.verify(secondary).reset();
  }
}
