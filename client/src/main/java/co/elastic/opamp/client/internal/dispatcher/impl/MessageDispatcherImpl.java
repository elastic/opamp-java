package co.elastic.opamp.client.internal.dispatcher.impl;

import co.elastic.opamp.client.internal.dispatcher.Message;
import co.elastic.opamp.client.internal.dispatcher.MessageBuilder;
import co.elastic.opamp.client.internal.dispatcher.MessageDispatcher;
import co.elastic.opamp.client.internal.dispatcher.ResponseHandler;
import co.elastic.opamp.client.request.HttpErrorException;
import co.elastic.opamp.client.request.MessageSender;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import opamp.proto.Opamp;

public class MessageDispatcherImpl implements MessageDispatcher, Runnable, MessageSender.Callback {
  private final MessageSender sender;
  private final ScheduledExecutorService executor;
  private MessageBuilder messageBuilder;
  private ResponseHandler responseHandler;
  private Future<?> currentSchedule;

  public static MessageDispatcher create(MessageSender sender) {
    ScheduledThreadPoolExecutor executor =
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    executor.setRemoveOnCancelPolicy(true);
    return new MessageDispatcherImpl(sender, executor);
  }

  MessageDispatcherImpl(MessageSender sender, ScheduledExecutorService executor) {
    this.sender = sender;
    this.executor = executor;
  }

  @Override
  public synchronized void dispatchWithDelay(long delay, TimeUnit unit) {
    clearSchedule();
    currentSchedule = executor.schedule(this, delay, unit);
  }

  @Override
  public synchronized void dispatchNow() {
    clearSchedule();
    currentSchedule = executor.submit(this);
  }

  @Override
  public void run() {
    clearSchedule();
    Message message = messageBuilder.buildMessage();
    if (message == null) {
      return;
    }

    sender.send(message.agentToServer, this);
  }

  @Override
  public void setMessageBuilder(MessageBuilder messageBuilder) {
    this.messageBuilder = messageBuilder;
  }

  @Override
  public void setResponseHandler(ResponseHandler responseHandler) {
    this.responseHandler = responseHandler;
  }

  private synchronized void clearSchedule() {
    if (currentSchedule != null && !currentSchedule.isDone() && !currentSchedule.isCancelled()) {
      currentSchedule.cancel(false);
    }
    currentSchedule = null;
  }

  @Override
  public void onSuccess(Opamp.ServerToAgent response) {
    responseHandler.handleSuccess(response);
  }

  @Override
  public void onFailure(int code, String message) {
    responseHandler.handleError(new HttpErrorException(code, message));
  }

  @Override
  public void onException(Throwable throwable) {
    responseHandler.handleError(throwable);
  }
}
