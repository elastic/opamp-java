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

import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.opamp.client.internal.request.fields.appenders.AgentDescriptionAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.AgentDisconnectAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.AgentToServerAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.AgentToServerAppenders;
import co.elastic.opamp.client.internal.request.fields.appenders.CapabilitiesAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.EffectiveConfigAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.FlagsAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.InstanceUidAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.RemoteConfigStatusAppender;
import co.elastic.opamp.client.internal.request.fields.appenders.SequenceNumberAppender;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentToServerAppendersTest {
  @Mock private AgentDescriptionAppender agentDescriptionAppender;
  @Mock private EffectiveConfigAppender effectiveConfigAppender;
  @Mock private RemoteConfigStatusAppender remoteConfigStatusAppender;
  @Mock private SequenceNumberAppender sequenceNumberAppender;
  @Mock private CapabilitiesAppender capabilitiesAppender;
  @Mock private FlagsAppender flagsAppender;
  @Mock private InstanceUidAppender instanceUidAppender;
  @Mock private AgentDisconnectAppender agentDisconnectAppender;
  @InjectMocks private AgentToServerAppenders appenders;

  @Test
  void verifyAppenderList() {
    assertThat(appenders.asList())
        .extracting("class")
        .containsExactlyInAnyOrderElementsOf(getAppendersFromParams());
  }

  @SuppressWarnings("unchecked")
  private List<Class<? extends AgentToServerAppender>> getAppendersFromParams() {
    List<Class<? extends AgentToServerAppender>> appenderTypes = new ArrayList<>();
    for (Field field : AgentToServerAppenders.class.getFields()) {
      if (AgentToServerAppender.class.isAssignableFrom(field.getType())) {
        appenderTypes.add((Class<? extends AgentToServerAppender>) field.getType());
      }
    }
    return appenderTypes;
  }
}
