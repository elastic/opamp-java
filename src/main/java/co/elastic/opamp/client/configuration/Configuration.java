package co.elastic.opamp.client.configuration;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Configuration {
  private final AtomicBoolean enabled = new AtomicBoolean(false);

  public boolean isEnabled() {
    return enabled.get();
  }

  public void setEnabled(boolean enabled) {
    this.enabled.set(enabled);
  }

  public String toJson() {
    return "{\"enabled\":" + enabled.get() + "}";
  }
}
