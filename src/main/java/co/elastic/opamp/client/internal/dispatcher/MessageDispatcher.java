package co.elastic.opamp.client.internal.dispatcher;

import co.elastic.opamp.client.request.HttpService;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import opamp.proto.Opamp;

public final class MessageDispatcher implements Runnable {
  private final AtomicBoolean isStarted = new AtomicBoolean(false);
  private final HttpService service;
  private MessageBuilder messageBuilder;
  private ResponseHandler responseHandler;
  private ScheduledExecutorService executor;

  public MessageDispatcher(HttpService service) {
    this.service = service;
  }

  public void start() {
    if (!isStarted.compareAndSet(false, true)) {
      executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleAtFixedRate(this, 0, 30, TimeUnit.SECONDS);
    }
  }

  public void stop() {
    if (isStarted.compareAndSet(true, false)) {
      executor.shutdown();
    }
  }

  @Override
  public void run() {
    Message message = messageBuilder.buildMessage();
    if (message == null) {
      return;
    }

    try {
      Opamp.ServerToAgent serverToAgent = service.sendMessage(message.agentToServer);
      responseHandler.handleResponse(serverToAgent);
    } catch (IOException e) {
      responseHandler.handleError(e);
    }
  }

  public void setMessageBuilder(MessageBuilder messageBuilder) {
    this.messageBuilder = messageBuilder;
  }

  public void setResponseHandler(ResponseHandler responseHandler) {
    this.responseHandler = responseHandler;
  }
}
