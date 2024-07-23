package co.elastic.opamp.client.request;

/**
 * Exception provided inside a {@link co.elastic.opamp.client.request.RequestSender.Response.Error}
 * response from a {@link RequestSender}.
 */
public class HttpErrorException extends Exception {
  public final int errorCode;

  /**
   * Constructs an HTTP error related exception.
   *
   * @param errorCode The HTTP error code.
   * @param message The HTTP error message associated with the code.
   */
  public HttpErrorException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
