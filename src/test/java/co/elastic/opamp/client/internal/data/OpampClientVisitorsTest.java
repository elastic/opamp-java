package co.elastic.opamp.client.internal.data;

import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.opamp.client.internal.visitors.AgentDescriptionVisitor;
import co.elastic.opamp.client.internal.visitors.AgentDisconnectVisitor;
import co.elastic.opamp.client.internal.visitors.AgentToServerVisitor;
import co.elastic.opamp.client.internal.visitors.CapabilitiesVisitor;
import co.elastic.opamp.client.internal.visitors.EffectiveConfigVisitor;
import co.elastic.opamp.client.internal.visitors.FlagsVisitor;
import co.elastic.opamp.client.internal.visitors.InstanceUidVisitor;
import co.elastic.opamp.client.internal.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.visitors.RemoteConfigStatusVisitor;
import co.elastic.opamp.client.internal.visitors.SequenceNumberVisitor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
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
        .extracting("class")
        .containsExactlyInAnyOrderElementsOf(getVisitorsFromParams());
  }

  @SuppressWarnings("unchecked")
  private List<Class<? extends AgentToServerVisitor>> getVisitorsFromParams() {
    List<Class<? extends AgentToServerVisitor>> visitorTypes = new ArrayList<>();
    for (Parameter parameter : OpampClientVisitors.class.getConstructors()[0].getParameters()) {
      if (AgentToServerVisitor.class.isAssignableFrom(parameter.getType())) {
        visitorTypes.add((Class<? extends AgentToServerVisitor>) parameter.getType());
      }
    }
    return visitorTypes;
  }
}
