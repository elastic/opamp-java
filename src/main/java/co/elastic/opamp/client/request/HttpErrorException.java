package co.elastic.opamp.client.request;

public class HttpErrorException extends Exception {
  public final int errorCode;

  public HttpErrorException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
