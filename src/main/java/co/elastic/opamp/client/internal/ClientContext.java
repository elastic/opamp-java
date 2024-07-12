package co.elastic.opamp.client.internal;

public final class ClientContext {
  public final boolean stop;
  public final boolean disableCompression;

  public ClientContext(boolean stop, boolean disableCompression) {
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

    public ClientContext buildAndReset() {
      ClientContext clientContext = new ClientContext(stop, disableCompression);
      reset();
      return clientContext;
    }

    private void reset() {
      stop = false;
      disableCompression = false;
    }
  }
}
