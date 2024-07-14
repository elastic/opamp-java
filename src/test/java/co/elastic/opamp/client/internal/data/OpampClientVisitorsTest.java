package co.elastic.opamp.client.internal.data;

import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpampClientVisitorsTest {
  @Mock private AgentDescriptionVisitor agentDescriptionVisitor;
  @Mock private EffectiveConfigVisitor effectiveConfigVisitor;
  @Mock private RemoteConfigStatusVisitor remoteConfigStatusVisitor;
  @Mock private SequenceNumberVisitor sequenceNumberVisitor;
  @Mock private CapabilitiesVisitor capabilitiesVisitor;
  @Mock private FlagsVisitor flagsVisitor;
  @Mock private InstanceUidVisitor instanceUidVisitor;
  @Mock private AgentDisconnectVisitor agentDisconnectVisitor;
  @InjectMocks private OpampClientVisitors visitors;

  @Test
  void verifyVisitorList() {
    assertThat(visitors.asList())
        .containsExactlyInAnyOrder(
            agentDescriptionVisitor,
            effectiveConfigVisitor,
            remoteConfigStatusVisitor,
            sequenceNumberVisitor,
            capabilitiesVisitor,
            flagsVisitor,
            instanceUidVisitor,
            agentDisconnectVisitor);
  }
}
