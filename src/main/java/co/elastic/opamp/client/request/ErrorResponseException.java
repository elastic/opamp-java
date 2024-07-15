package co.elastic.opamp.client.request;

public class ErrorResponseException extends Exception {
  public final int errorCode;

  public ErrorResponseException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
