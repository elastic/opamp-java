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
package co.elastic.opamp.client.internal.request.fields.appenders;

import co.elastic.opamp.client.internal.request.fields.FieldType;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public final class AgentDescriptionAppender implements AgentToServerAppender {
  private final Supplier<Opamp.AgentDescription> data;

  public static AgentDescriptionAppender create(Supplier<Opamp.AgentDescription> data) {
    return new AgentDescriptionAppender(data);
  }

  private AgentDescriptionAppender(Supplier<Opamp.AgentDescription> data) {
    this.data = data;
  }

  @Override
  public void appendTo(Opamp.AgentToServer.Builder builder) {
    builder.setAgentDescription(data.get());
  }

  @Override
  public FieldType getFieldType() {
    return FieldType.AGENT_DESCRIPTION;
  }
}