package com.remondis.limbus;

/**
 * Thrown if a Limbus specification or a documented constraint was violated. This error is not used in public APIs. APIs
 * will validate the constraints and method arguments accordingly. This error is thrown by internal methods were the
 * constraints can only violate if there is a bug that makes certain conditions invalid.
 *
 * @author schuettec
 *
 */
public class LimbusConventionError extends Error {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LimbusConventionError() {
  }

  public LimbusConventionError(String message) {
    super(message);
  }

  public LimbusConventionError(Throwable cause) {
    super(cause);
  }

  public LimbusConventionError(String message, Throwable cause) {
    super(message, cause);
  }

  public LimbusConventionError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
