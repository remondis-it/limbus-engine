package com.remondis.limbus.api;

/**
 * Thrown if something is wrong with the Limbus class path configuration.
 *
 * @author schuettec
 *
 */
public class LimbusClasspathException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LimbusClasspathException() {
  }

  public LimbusClasspathException(String message) {
    super(message);
  }

  public LimbusClasspathException(Throwable cause) {
    super(cause);
  }

  public LimbusClasspathException(String message, Throwable cause) {
    super(message, cause);
  }

  public LimbusClasspathException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
