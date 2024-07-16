package co.elastic.opamp.client.internal.tools;

import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.RemoteConfigStatusState;
import co.elastic.opamp.client.internal.state.StateHolder;
import co.elastic.opamp.client.internal.state.observer.Observable;
import co.elastic.opamp.client.internal.state.observer.Observer;
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

    observe(state.remoteConfigStatusState, watcher);

    return watcher;
  }

  private static void observe(StateHolder<?> state, ResponseActionsWatcher watcher) {
    state.addObserver(watcher);
    watcher.observables.add(state);
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
