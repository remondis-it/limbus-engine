package com.remondis.limbus.monitoring;

/**
 * Thrown if a publisher implementation does not met the requirements for a valid publisher.
 *
 * @author schuettec
 *
 */
public class InvalidPublisherException extends RuntimeException {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private InvalidPublisherException() {
  }

  private InvalidPublisherException(String message) {
    super(message);
  }

  private InvalidPublisherException(Throwable cause) {
    super(cause);
  }

  private InvalidPublisherException(String message, Throwable cause) {
    super(message, cause);
  }

  private InvalidPublisherException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public static InvalidPublisherException noPublisherInterfaces(Class<?> implementation) {
    return new InvalidPublisherException(
        String.format("The specified publisher implementation '%s' does not implement a publisher interface.",
            implementation.getName()));
  }

}
