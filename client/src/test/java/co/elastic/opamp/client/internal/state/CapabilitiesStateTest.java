package co.elastic.opamp.client.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CapabilitiesStateTest {
  private CapabilitiesState capabilitiesState;

  @BeforeEach
  void setUp() {
    capabilitiesState = CapabilitiesState.create();
  }

  @Test
  void verifyDefaultValue() {
    assertThat(hasFlag(Opamp.AgentCapabilities.AgentCapabilities_ReportsStatus_VALUE)).isTrue();
  }

  @Test
  void verifyAddingAndRemovingCapabilities() {
    int capability = Opamp.AgentCapabilities.AgentCapabilities_AcceptsPackages_VALUE;
    assertThat(hasFlag(capability)).isFalse();

    capabilitiesState.add(capability);
    assertThat(hasFlag(capability)).isTrue();
    assertThat(hasFlag(Opamp.AgentCapabilities.AgentCapabilities_ReportsStatus_VALUE)).isTrue();

    capabilitiesState.remove(capability);
    assertThat(hasFlag(capability)).isFalse();
    assertThat(hasFlag(Opamp.AgentCapabilities.AgentCapabilities_ReportsStatus_VALUE)).isTrue();
  }

  private boolean hasFlag(long flag) {
    return (capabilitiesState.get() & flag) == flag;
  }
}
