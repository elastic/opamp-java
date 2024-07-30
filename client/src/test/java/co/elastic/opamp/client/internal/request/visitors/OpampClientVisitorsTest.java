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
package co.elastic.opamp.client.internal.request.visitors;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpampClientVisitorsTest {
  @Mock private AgentDescriptionVisitor agentDescriptionVisitor;
  @Mock private EffectiveConfigVisitor effectiveConfigVisitor;
  @Mock private RemoteConfigStatusVisitor remoteConfigStatusVisitor;
  @Mock private SequenceNumberVisitor sequenceNumberVisitor;
  @Mock private CapabilitiesVisitor capabilitiesVisitor;
  @Mock private FlagsVisitor flagsVisitor;
  @Mock private InstanceUidVisitor instanceUidVisitor;
  @Mock private AgentDisconnectVisitor agentDisconnectVisitor;
  @InjectMocks private OpampClientVisitors visitors;

  @Test
  void verifyVisitorList() {
    assertThat(visitors.asList())
        .extracting("class")
        .containsExactlyInAnyOrderElementsOf(getVisitorsFromParams());
  }

  @SuppressWarnings("unchecked")
  private List<Class<? extends AgentToServerVisitor>> getVisitorsFromParams() {
    List<Class<? extends AgentToServerVisitor>> visitorTypes = new ArrayList<>();
    for (Parameter parameter : OpampClientVisitors.class.getConstructors()[0].getParameters()) {
      if (AgentToServerVisitor.class.isAssignableFrom(parameter.getType())) {
        visitorTypes.add((Class<? extends AgentToServerVisitor>) parameter.getType());
      }
    }
    return visitorTypes;
  }
}
