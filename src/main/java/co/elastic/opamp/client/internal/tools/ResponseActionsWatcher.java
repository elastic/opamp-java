package co.elastic.opamp.client.internal.tools;

import co.elastic.opamp.client.internal.state.Observable;
import co.elastic.opamp.client.internal.state.Observer;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.RemoteConfigStatusState;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import opamp.proto.Opamp;

public final class ResponseActionsWatcher implements Observer, Closeable {
  private final Opamp.ServerToAgent response;
  private final List<Observable> observables = new ArrayList<>();
  private boolean remoteConfigChanged = false;

  public static ResponseActionsWatcher create(
      Opamp.ServerToAgent response, OpampClientState state) {
    ResponseActionsWatcher watcher = new ResponseActionsWatcher(response);
    state.remoteConfigStatusState.addObserver(watcher);
    watcher.observables.add(state.remoteConfigStatusState);

    return watcher;
  }

  private ResponseActionsWatcher(Opamp.ServerToAgent response) {
    this.response = response;
  }

  @Override
  public void update(Observable observable) {
    if (observable instanceof RemoteConfigStatusState) {
      remoteConfigChanged = true;
    }
  }

  public boolean stateHasChanged() {
    if (response.hasRemoteConfig() && remoteConfigChanged) {
      return true;
    }

    return false;
  }

  @Override
  public void close() {
    for (Observable observable : observables) {
      observable.removeObserver(this);
    }
  }
}
