package co.elastic.opamp.client.handlers;

import java.util.function.Supplier;

public interface InstanceUidHandler extends Supplier<byte[]> {

  static InstanceUidHandler getDefault() {
    return new DefaultInstanceUidHandler();
  }
}
