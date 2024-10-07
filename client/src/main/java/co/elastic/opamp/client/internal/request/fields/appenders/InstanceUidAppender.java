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
import com.google.protobuf.ByteString;
import java.util.function.Supplier;
import opamp.proto.Opamp;

public final class InstanceUidAppender implements AgentToServerAppender {
  private final Supplier<byte[]> instanceUid;

  public static InstanceUidAppender create(Supplier<byte[]> instanceUid) {
    return new InstanceUidAppender(instanceUid);
  }

  private InstanceUidAppender(Supplier<byte[]> instanceUid) {
    this.instanceUid = instanceUid;
  }

  @Override
  public void appendTo(Opamp.AgentToServer.Builder builder) {
    builder.setInstanceUid(ByteString.copyFrom(instanceUid.get()));
  }

  @Override
  public FieldType getFieldType() {
    return FieldType.INSTANCE_UID;
  }
}