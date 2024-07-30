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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OpampClientVisitors {
  private final List<AgentToServerVisitor> allVisitors;

  public OpampClientVisitors(
      AgentDescriptionVisitor agentDescriptionVisitor,
      EffectiveConfigVisitor effectiveConfigVisitor,
      RemoteConfigStatusVisitor remoteConfigStatusVisitor,
      SequenceNumberVisitor sequenceNumberVisitor,
      CapabilitiesVisitor capabilitiesVisitor,
      InstanceUidVisitor instanceUidVisitor,
      FlagsVisitor flagsVisitor,
      AgentDisconnectVisitor agentDisconnectVisitor) {
    List<AgentToServerVisitor> visitors = new ArrayList<>();
    visitors.add(agentDescriptionVisitor);
    visitors.add(effectiveConfigVisitor);
    visitors.add(remoteConfigStatusVisitor);
    visitors.add(sequenceNumberVisitor);
    visitors.add(capabilitiesVisitor);
    visitors.add(instanceUidVisitor);
    visitors.add(flagsVisitor);
    visitors.add(agentDisconnectVisitor);
    allVisitors = Collections.unmodifiableList(visitors);
  }

  public List<AgentToServerVisitor> asList() {
    return allVisitors;
  }
}
