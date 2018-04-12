package com.remondis.limbus.exceptions;

/**
 * This is the most general exception type for the Limbus API.
 *
 * @author schuettec
 *
 */
public class LimbusException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LimbusException() {
  }

  public LimbusException(String message) {
    super(message);
  }

  public LimbusException(Throwable cause) {
    super(cause);
  }

  public LimbusException(String message, Throwable cause) {
    super(message, cause);
  }

  public LimbusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
