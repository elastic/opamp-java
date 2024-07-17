package co.elastic.opamp.client.handlers.impl;

import co.elastic.opamp.client.handlers.InstanceUidHandler;
import com.github.f4b6a3.uuid.UuidCreator;
import java.nio.ByteBuffer;
import java.util.UUID;

public class DefaultInstanceUidHandler implements InstanceUidHandler {
  private byte[] uuid;

  @Override
  public byte[] get() {
    if (uuid == null) {
      UUID random = UuidCreator.getTimeOrderedEpoch();
      ByteBuffer buffer = ByteBuffer.allocate(16);
      buffer.putLong(random.getMostSignificantBits());
      buffer.putLong(random.getLeastSignificantBits());
      uuid = buffer.array();
    }

    return uuid;
  }
}
