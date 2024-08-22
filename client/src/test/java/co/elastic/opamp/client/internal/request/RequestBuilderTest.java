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
package co.elastic.opamp.client.internal.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import co.elastic.opamp.client.internal.request.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class RequestBuilderTest {
  private OpampClientVisitors visitors;
  private RequestBuilder requestBuilder;

  @BeforeEach
  void setUp() {
    visitors = mock();
    requestBuilder = new RequestBuilder(visitors);
  }

  @Test
  void verifyRequestBuilding() {
    AgentToServerVisitor mockVisitor1 = mock();
    AgentToServerVisitor mockVisitor2 = mock();
    ArgumentCaptor<RequestContext> contextCaptor = ArgumentCaptor.forClass(RequestContext.class);
    doReturn(List.of(mockVisitor1, mockVisitor2)).when(visitors).asList();

    requestBuilder.build();

    InOrder inOrder = inOrder(mockVisitor1, mockVisitor2);
    inOrder.verify(mockVisitor1).visit(contextCaptor.capture(), notNull());
    inOrder.verify(mockVisitor2).visit(contextCaptor.capture(), notNull());
    List<RequestContext> capturedContexts = contextCaptor.getAllValues();
    assertThat(capturedContexts).hasSize(2);
    assertThat(capturedContexts.get(0)).isEqualTo(capturedContexts.get(1));
  }
}
