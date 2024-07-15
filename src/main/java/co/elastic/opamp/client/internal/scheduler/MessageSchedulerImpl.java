package co.elastic.opamp.client.internal.scheduler;

import co.elastic.opamp.client.request.HttpErrorException;
import co.elastic.opamp.client.request.RequestCallback;
import co.elastic.opamp.client.request.Service;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import opamp.proto.Opamp;

class MessageSchedulerImpl implements MessageScheduler, Runnable, RequestCallback {
  private final Service service;
  private final ScheduledExecutorService executor;
  private MessageBuilder messageBuilder;
  private ResponseHandler responseHandler;
  private Future<?> currentSchedule;

  static MessageScheduler create(Service service) {
    ScheduledThreadPoolExecutor executor =
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    executor.setRemoveOnCancelPolicy(true);
    return new MessageSchedulerImpl(service, executor);
  }

  MessageSchedulerImpl(Service service, ScheduledExecutorService executor) {
    this.service = service;
    this.executor = executor;
  }

  @Override
  public synchronized void scheduleWithDelay(long delay, TimeUnit unit) {
    clearSchedule();
    currentSchedule = executor.schedule(this, delay, unit);
  }

  @Override
  public synchronized void scheduleNow() {
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

    service.sendMessage(message.agentToServer, this);
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
