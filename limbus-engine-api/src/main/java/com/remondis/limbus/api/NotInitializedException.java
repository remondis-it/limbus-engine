/**
 *
 */
package com.remondis.limbus.api;

/**
 * @author schuettec
 *
 */
public class NotInitializedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public NotInitializedException() {
    super();
  }

  public NotInitializedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public NotInitializedException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotInitializedException(String message) {
    super(message);
  }

  public NotInitializedException(Throwable cause) {
    super(cause);
  }

}
