package co.elastic.opamp.client.request.schedule;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.opamp.client.internal.request.schedule.DualSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DualScheduleTest {
  @Mock private Schedule main;
  @Mock private Schedule secondary;
  private DualSchedule dualSchedule;

  @BeforeEach
  void setUp() {
    dualSchedule = DualSchedule.of(main, secondary);
  }

  @Test
  void verifyDefaultSchedule() {
    dualSchedule.fastForward();
    dualSchedule.start();
    dualSchedule.isDue();

    InOrder inOrder = inOrder(main);
    inOrder.verify(main).fastForward();
    inOrder.verify(main).start();
    inOrder.verify(main).isDue();
    verifyNoInteractions(secondary);
  }

  @Test
  void verifySwitchToSecondary() {
    dualSchedule.switchToSecondary();
    dualSchedule.fastForward();
    dualSchedule.start();
    dualSchedule.isDue();

    InOrder inOrder = inOrder(secondary);
    inOrder.verify(secondary).fastForward();
    inOrder.verify(secondary).start();
    inOrder.verify(secondary).isDue();
  }
}
