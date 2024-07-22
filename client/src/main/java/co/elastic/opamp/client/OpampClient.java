package co.elastic.opamp.client;

import co.elastic.opamp.client.response.MessageData;
import opamp.proto.Opamp;

public interface OpampClient {

  static OpampClientBuilder builder() {
    return new OpampClientBuilder();
  }

  /**
   * Starts the client and begin attempts to connect to the Server. Once connection is established
   * the client will attempt to maintain it by reconnecting if the connection is lost. All failed
   * connection attempts will be reported via {@link Callback#onConnectFailed(OpampClient,
   * Throwable)} callback.
   *
   * <p>This method does not wait until the connection to the Server is established and will likely
   * return before the connection attempts are even made.
   *
   * <p>This method may be called only once.
   */
  void start();

  /**
   * Stops the client. May be called only after {@link #start()}. May be called only once. Once
   * stopped, the client cannot be started again.
   */
  void stop();

  /**
   * Sets the current remote config status which will be sent in the next agent to server request.
   *
   * @param remoteConfigStatus - The new remote config status.
   */
  void setRemoteConfigStatus(Opamp.RemoteConfigStatus remoteConfigStatus);

  /**
   * Sets the current effective config which will be sent in the next agent to server request.
   *
   * @param effectiveConfig - The new effective config.
   */
  void setEffectiveConfig(Opamp.EffectiveConfig effectiveConfig);

  interface Callback {
    void onConnect(OpampClient client);

    void onConnectFailed(OpampClient client, Throwable throwable);

    void onErrorResponse(OpampClient client, Opamp.ServerErrorResponse errorResponse);

    void onMessage(OpampClient client, MessageData messageData);
  }
}
