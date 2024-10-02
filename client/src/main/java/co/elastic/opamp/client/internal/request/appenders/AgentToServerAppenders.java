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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AgentToServerAppenders {
  private final List<AgentToServerAppender> allAppenders;

  public AgentToServerAppenders(
      AgentDescriptionAppender agentDescriptionAppender,
      EffectiveConfigAppender effectiveConfigAppender,
      RemoteConfigStatusAppender remoteConfigStatusAppender,
      SequenceNumberAppender sequenceNumberAppender,
      CapabilitiesAppender capabilitiesAppender,
      InstanceUidAppender instanceUidAppender,
      FlagsAppender flagsAppender,
      AgentDisconnectAppender agentDisconnectAppender) {
    List<AgentToServerAppender> appenders = new ArrayList<>();
    appenders.add(agentDescriptionAppender);
    appenders.add(effectiveConfigAppender);
    appenders.add(remoteConfigStatusAppender);
    appenders.add(sequenceNumberAppender);
    appenders.add(capabilitiesAppender);
    appenders.add(instanceUidAppender);
    appenders.add(flagsAppender);
    appenders.add(agentDisconnectAppender);
    allAppenders = Collections.unmodifiableList(appenders);
  }

  public List<AgentToServerAppender> asList() {
    return allAppenders;
  }
}
