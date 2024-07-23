package co.elastic.opamp.client.internal.state;

import opamp.proto.Opamp;

public final class CapabilitiesState extends StateHolder<Long> {

  static CapabilitiesState create() {
    return new CapabilitiesState(Opamp.AgentCapabilities.AgentCapabilities_ReportsStatus_VALUE);
  }

  public void add(long capabilities) {
    set(get() | capabilities);
  }

  public void remove(long capabilities) {
    set(get() & ~capabilities);
  }

  private CapabilitiesState(long initialState) {
    super(initialState);
  }
}
