package org.max5.limbus.exceptions;

public class NoSuchDeploymentException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public NoSuchDeploymentException() {
  }

  public NoSuchDeploymentException(String message) {
    super(message);
  }

  public NoSuchDeploymentException(Throwable cause) {
    super(cause);
  }

  public NoSuchDeploymentException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoSuchDeploymentException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * @return Returns a new {@link NoSuchDeploymentException} using a default message.
   */
  public static NoSuchDeploymentException createDefault() {
    return new NoSuchDeploymentException("The specified classpath is not deployed on this container.");
  }

}
