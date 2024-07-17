package co.elastic.opamp.client.internal.request;

public final class RequestContext {
  public final boolean stop;
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
