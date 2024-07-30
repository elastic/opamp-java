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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import co.elastic.opamp.client.internal.request.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class RequestBuilderTest {
  private OpampClientVisitors visitors;
  private RequestContext.Builder contextBuilder;
  private Supplier<RequestContext.Builder> contextBuilderSupplier;
  private RequestBuilder requestBuilder;

  @BeforeEach
  void setUp() {
    visitors = mock();
    contextBuilder = mock();
    contextBuilderSupplier = mock();
    doReturn(contextBuilder).when(contextBuilderSupplier).get();
    requestBuilder = new RequestBuilder(visitors, contextBuilderSupplier);
  }

  @Test
  void verifyRequestBuilding() {
    RequestContext requestContext = mock();
    doReturn(requestContext).when(contextBuilder).build();
    AgentToServerVisitor mockVisitor1 = mock();
    AgentToServerVisitor mockVisitor2 = mock();
    doReturn(List.of(mockVisitor1, mockVisitor2)).when(visitors).asList();
    clearInvocations(contextBuilderSupplier);

    requestBuilder.buildAndReset();

    InOrder inOrder = inOrder(contextBuilder, mockVisitor1, mockVisitor2, contextBuilderSupplier);
    inOrder.verify(contextBuilder).build();
    inOrder.verify(mockVisitor1).visit(eq(requestContext), notNull());
    inOrder.verify(mockVisitor2).visit(eq(requestContext), notNull());
    inOrder.verify(contextBuilderSupplier).get();
  }
}
