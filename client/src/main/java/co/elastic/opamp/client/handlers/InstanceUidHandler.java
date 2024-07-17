package co.elastic.opamp.client.handlers;

import co.elastic.opamp.client.handlers.impl.DefaultInstanceUidHandler;
import java.util.function.Supplier;

public interface InstanceUidHandler extends Supplier<byte[]> {

  static InstanceUidHandler getDefault() {
    return new DefaultInstanceUidHandler();
  }
}
