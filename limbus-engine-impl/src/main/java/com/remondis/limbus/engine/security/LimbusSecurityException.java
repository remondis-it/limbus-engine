package com.remondis.limbus.engine.security;

/**
 * Thrown if the Limbus security sandbox cannot be initialized properly.
 *
 * @author schuettec
 *
 */
public class LimbusSecurityException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LimbusSecurityException() {
    super();
  }

  public LimbusSecurityException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public LimbusSecurityException(String message, Throwable cause) {
    super(message, cause);
  }

  public LimbusSecurityException(String message) {
    super(message);
  }

  public LimbusSecurityException(Throwable cause) {
    super(cause);
  }

}
