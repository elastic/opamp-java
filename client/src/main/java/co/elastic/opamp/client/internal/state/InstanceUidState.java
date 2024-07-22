package co.elastic.opamp.client.internal.state;

import com.github.f4b6a3.uuid.UuidCreator;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class InstanceUidState extends StateHolder<byte[]> {

  public static InstanceUidState createRandom() {
    UUID uuid = UuidCreator.getTimeOrderedEpoch();
    ByteBuffer buffer = ByteBuffer.allocate(16);
    buffer.putLong(uuid.getMostSignificantBits());
    buffer.putLong(uuid.getLeastSignificantBits());
    return create(buffer.array());
  }

  public static InstanceUidState create(byte[] uuid) {
    return new InstanceUidState(uuid);
  }

  private InstanceUidState(byte[] initialState) {
    super(initialState);
  }

  @Override
  protected boolean areEqual(byte[] first, byte[] second) {
    return Arrays.equals(first, second);
  }
}
