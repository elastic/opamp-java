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
