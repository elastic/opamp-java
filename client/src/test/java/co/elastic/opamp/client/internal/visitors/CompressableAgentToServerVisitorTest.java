package co.elastic.opamp.client.internal.visitors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import co.elastic.opamp.client.internal.RequestContext;
import opamp.proto.Opamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

class CompressableAgentToServerVisitorTest {
  private CompressableAgentToServerVisitor visitor;

  @BeforeEach
  void setUp() {
    visitor =
        new CompressableAgentToServerVisitor() {
          @Override
          protected void doVisit(
              RequestContext requestContext, Opamp.AgentToServer.Builder builder) {}
        };
  }

  @Test
  void doVisitFirstTime() {
    verifyVisit();
  }

  @Test
  void verifyDoVisitOnceWithoutUpdating() {
    verifyVisit();

    verifyVisit(mock(), times(0));
  }

  @Test
  void verifyDoVisitAfterUpdate() {
    verifyVisit();

    visitor.update(null);

    verifyVisit();
  }

  @Test
  void verifyDoVisitWhenServerRequiresIt() {
    verifyVisit();

    verifyVisit(RequestContext.newBuilder().disableCompression().buildAndReset());
  }

  private void verifyVisit() {
    verifyVisit(mock());
  }

  private void verifyVisit(RequestContext context) {
    verifyVisit(context, times(1));
  }

  private void verifyVisit(RequestContext context, VerificationMode verificationMode) {
    Opamp.AgentToServer.Builder builder = Opamp.AgentToServer.newBuilder();
    CompressableAgentToServerVisitor spy = spy(visitor);

    spy.visit(context, builder);

    verify(spy, verificationMode).doVisit(context, builder);
  }
}
