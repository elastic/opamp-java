package co.elastic.opamp.client.internal.request;

/**
 * Information that affects how a request is built. Used by implementations of {@link
 * co.elastic.opamp.client.internal.request.visitors.AgentToServerVisitor}.
 */
public final class RequestContext {
  /**
   * This is set to {@link Boolean#TRUE} when the request being built is the last one that the
   * Client will send after it has being stopped. As explained <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#closing-connection">here</a>.
   */
  public final boolean stop;

  /**
   * This is set to {@link Boolean#TRUE} when the Server has requested the Client to report a full
   * state, as explained <a
   * href="https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agent-status-compression">here</a>.
   */
  public final boolean disableCompression;

  public RequestContext(boolean stop, boolean disableCompression) {
    this.stop = stop;
    this.disableCompression = disableCompression;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private boolean stop = false;
    private boolean disableCompression = false;

    private Builder() {}

    public Builder stop() {
      stop = true;
      return this;
    }

    public Builder disableCompression() {
      disableCompression = true;
      return this;
    }

    public RequestContext build() {
      return new RequestContext(stop, disableCompression);
    }
  }
}
