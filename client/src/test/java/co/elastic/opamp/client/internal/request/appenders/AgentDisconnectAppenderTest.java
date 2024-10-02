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
package co.elastic.opamp.client.internal.request.appenders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.internal.request.RequestContext;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentDisconnectAppenderTest {
  private AgentDisconnectAppender agentDisconnectAppender;

  @BeforeEach
  void setUp() {
    agentDisconnectAppender = AgentDisconnectAppender.create();
  }

  @Test
  void whenStopEnabled_sendDisconnectMessage() {
    Opamp.AgentToServer.Builder builder = mock();

    agentDisconnectAppender.visit(RequestContext.builder().setStop(true).build(), builder);

    verify(builder).setAgentDisconnect((Opamp.AgentDisconnect) notNull());
  }

  @Test
  void whenStopNotEnabled_doNotSendDisconnectMessage() {
    Opamp.AgentToServer.Builder builder = mock();

    agentDisconnectAppender.visit(RequestContext.builder().build(), builder);

    verify(builder, never()).setAgentDisconnect((Opamp.AgentDisconnect) any());
  }
}
