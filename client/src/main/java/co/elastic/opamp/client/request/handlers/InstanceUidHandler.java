package co.elastic.opamp.client.request.handlers;

import co.elastic.opamp.client.internal.request.handlers.DefaultInstanceUidHandler;
import java.util.function.Supplier;

public interface InstanceUidHandler extends Supplier<byte[]> {

  static InstanceUidHandler getDefault() {
    return new DefaultInstanceUidHandler();
  }
}
