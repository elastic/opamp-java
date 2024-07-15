package co.elastic.opamp.client.internal.state;

import opamp.proto.Opamp;

public class RemoteConfigStatusState extends StateHolder<Opamp.RemoteConfigStatus> {

  static RemoteConfigStatusState create() {
    return new RemoteConfigStatusState(
        Opamp.RemoteConfigStatus.newBuilder()
            .setStatus(Opamp.RemoteConfigStatuses.RemoteConfigStatuses_UNSET)
            .build());
  }

  private RemoteConfigStatusState(Opamp.RemoteConfigStatus initialState) {
    super(initialState);
  }
}
