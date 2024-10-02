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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.internal.request.RequestContext;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

class CompressableAgentToServerAppenderTest {
  private CompressableAgentToServerAppender appender;

  @BeforeEach
  void setUp() {
    appender =
        new CompressableAgentToServerAppender() {
          @Override
          protected void doVisit(
              RequestContext requestContext, Opamp.AgentToServer.Builder builder) {}
        };
  }

  @Test
  void doVisitFirstTime() {
    verifyVisit();
  }

  @Test
  void verifyDoVisitOnceWithoutUpdating() {
    verifyVisit();

    verifyVisit(mock(), times(0));
  }

  @Test
  void verifyDoVisitAfterUpdate() {
    verifyVisit();

    appender.update(null);

    verifyVisit();
  }

  @Test
  void verifyDoVisitWhenServerRequiresIt() {
    verifyVisit();

    verifyVisit(RequestContext.builder().setDisableCompression(true).build());
  }

  private void verifyVisit() {
    verifyVisit(mock());
  }

  private void verifyVisit(RequestContext context) {
    verifyVisit(context, times(1));
  }

  private void verifyVisit(RequestContext context, VerificationMode verificationMode) {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    CompressableAgentToServerAppender spy = spy(appender);

    spy.visit(context, builder);

    verify(spy, verificationMode).doVisit(context, builder);
  }
}
