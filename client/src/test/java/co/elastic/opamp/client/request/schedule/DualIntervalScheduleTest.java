package co.elastic.opamp.client.request.schedule;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.internal.request.schedule.DualIntervalSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DualIntervalScheduleTest {
  @Mock private IntervalSchedule main;
  @Mock private IntervalSchedule secondary;
  private DualIntervalSchedule dualSchedule;

  @BeforeEach
  void setUp() {
    dualSchedule = DualIntervalSchedule.of(main, secondary);
  }

  @Test
  void verifyDefaultSchedule() {
    dualSchedule.fastForward();
    dualSchedule.startNext();
    dualSchedule.isDue();
    dualSchedule.reset();

    InOrder inOrder = inOrder(main);
    inOrder.verify(main).fastForward();
    inOrder.verify(main).startNext();
    inOrder.verify(main).isDue();
    inOrder.verify(main).reset();
    verifyNoInteractions(secondary);
  }

  @Test
  void verifySwitchToSecondary() {
    dualSchedule.switchToSecondary();
    dualSchedule.fastForward();
    dualSchedule.startNext();
    dualSchedule.isDue();
    dualSchedule.reset();

    InOrder inOrder = inOrder(secondary);
    inOrder.verify(secondary).fastForward();
    inOrder.verify(secondary).startNext();
    inOrder.verify(secondary).isDue();
    inOrder.verify(secondary).reset();
  }
}
