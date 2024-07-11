package co.elastic.opamp.client.internal;

public final class ClientContext {
  public final boolean stop;
  public final boolean disableCompression;

  public static Builder newBuilder() {
    return new Builder();
  }

  private ClientContext(Builder builder) {
    this.stop = builder.stop;
    this.disableCompression = builder.disableCompression;
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

    public ClientContext build() {
      return new ClientContext(this);
    }
  }
}
