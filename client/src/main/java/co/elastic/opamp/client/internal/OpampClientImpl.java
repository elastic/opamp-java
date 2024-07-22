package co.elastic.opamp.client.internal;

import co.elastic.opamp.client.OpampClient;
import co.elastic.opamp.client.internal.request.RequestBuilder;
import co.elastic.opamp.client.internal.request.RequestDispatcher;
import co.elastic.opamp.client.internal.request.visitors.OpampClientVisitors;
import co.elastic.opamp.client.internal.state.OpampClientState;
import co.elastic.opamp.client.internal.state.observer.Observable;
import co.elastic.opamp.client.internal.state.observer.Observer;
import co.elastic.opamp.client.request.Request;
import co.elastic.opamp.client.request.RequestSender;
import co.elastic.opamp.client.request.handlers.IntervalHandler;
import co.elastic.opamp.client.response.MessageData;
import com.google.protobuf.ByteString;
import java.time.Duration;
import opamp.proto.Opamp;

public final class OpampClientImpl implements OpampClient, Observer, Runnable {
  private final RequestSender sender;
  private final RequestDispatcher dispatcher;
  private final RequestBuilder requestBuilder;
  private final OpampClientState state;
  private final Callback callback;
  private final Object runningLock = new Object();
  private boolean isRunning;
  private boolean isStopped;

  public static OpampClientImpl create(
      RequestSender sender,
      OpampClientVisitors visitors,
      OpampClientState state,
      IntervalHandler pollingInterval,
      IntervalHandler retryInterval,
      Callback callback) {
    RequestBuilder requestBuilder = RequestBuilder.create(visitors);
    RequestDispatcher dispatcher = RequestDispatcher.create(pollingInterval, retryInterval);
    return new OpampClientImpl(sender, dispatcher, requestBuilder, state, callback);
  }

  OpampClientImpl(
      RequestSender sender,
      RequestDispatcher dispatcher,
      RequestBuilder requestBuilder,
      OpampClientState state,
      Callback callback) {
    this.sender = sender;
    this.dispatcher = dispatcher;
    this.requestBuilder = requestBuilder;
    this.state = state;
    this.callback = callback;
  }

  @Override
  public void start() {
    synchronized (runningLock) {
      if (!isRunning) {
        isRunning = true;
        observeStatusChange();
        dispatcher.start(this);
      } else {
        throw new IllegalStateException("The client has already been started");
      }
    }
  }

  @Override
  public void stop() {
    synchronized (runningLock) {
      if (!isRunning) {
        throw new IllegalStateException("The client has not been started");
      }
      if (!isStopped) {
        isStopped = true;
        requestBuilder.stop();
        dispatcher.stop();
      } else {
        throw new IllegalStateException("The client has already been stopped");
      }
    }
  }

  @Override
  public void setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus) {
    state.remoteConfigStatusState.set(remoteConfigStatus);
  }

  @Override
  public void setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig) {
    state.effectiveConfigState.set(effectiveConfig);
  }

  private void onConnectionSuccess(Opamp.ServerToAgent response) {
    state.sequenceNumberState.increment();
    callback.onConnect(this);
    if (dispatcher.isRetryModeEnabled()) dispatcher.disableRetryMode();
    if (response == null) return;

    handleResponse(response);
  }

  private void onConnectionError(Throwable throwable) {
    callback.onConnectFailed(this, throwable);
    if (!dispatcher.isRetryModeEnabled()) dispatcher.enableRetryMode(null);
  }

  private void handleResponse(Opamp.ServerToAgent response) {
    if (response.hasErrorResponse()) {
      Opamp.ServerErrorResponse errorResponse = response.getErrorResponse();
      handleErrorResponse(errorResponse);
      callback.onErrorResponse(this, errorResponse);
    }
    long reportFullState = Opamp.ServerToAgentFlags.ServerToAgentFlags_ReportFullState_VALUE;
    if ((response.getFlags() & reportFullState) == reportFullState) {
      requestBuilder.disableCompression();
    }
    handleAgentIdentification(response);

    boolean notifyOnMessage = false;
    MessageData.Builder messageBuilder = MessageData.builder();

    if (response.hasRemoteConfig()) {
      notifyOnMessage = true;
      messageBuilder.setRemoteConfig(response.getRemoteConfig());
    }

    if (notifyOnMessage) {
      callback.onMessage(this, messageBuilder.build());
    }
  }

  private void handleAgentIdentification(Opamp.ServerToAgent response) {
    if (response.hasAgentIdentification()) {
      ByteString newInstanceUid = response.getAgentIdentification().getNewInstanceUid();
      if (!newInstanceUid.isEmpty()) {
        state.instanceUidState.set(newInstanceUid.toByteArray());
      }
    }
  }

  private void handleErrorResponse(Opamp.ServerErrorResponse errorResponse) {
    if (errorResponse.getType()
        == Opamp.ServerErrorResponseType.ServerErrorResponseType_Unavailable) {
      if (errorResponse.hasRetryInfo()) {
        long retryAfterNanoseconds = errorResponse.getRetryInfo().getRetryAfterNanoseconds();
        dispatcher.enableRetryMode(Duration.ofNanos(retryAfterNanoseconds));
      } else {
        dispatcher.enableRetryMode(null);
      }
    }
  }

  @Override
  public void run() {
    Request request = requestBuilder.buildAndReset();

    RequestSender.Response response = sender.send(request);

    if (response instanceof RequestSender.Response.Success) {
      onConnectionSuccess(((RequestSender.Response.Success) response).data);
    } else if (response instanceof RequestSender.Response.Error) {
      onConnectionError(((RequestSender.Response.Error) response).throwable);
    } else {
      throw new IllegalStateException("Unexpected response: " + response);
    }
  }

  @Override
  public void update(Observable observable) {
    // There was an agent status change.
    dispatcher.tryDispatchNow();
  }

  private void observeStatusChange() {
    state.agentDescriptionState.addObserver(this);
    state.effectiveConfigState.addObserver(this);
    state.remoteConfigStatusState.addObserver(this);
    state.capabilitiesState.addObserver(this);
    state.instanceUidState.addObserver(this);
  }
}
