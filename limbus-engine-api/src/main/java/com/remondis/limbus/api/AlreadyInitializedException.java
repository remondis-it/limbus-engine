/**
 *
 */
package com.remondis.limbus.api;

/**
 * This exception is thrown if the lifecycle methods of {@link Initializable} are not called in a legal order. Example:
 * This exception is thrown if an {@link Initializable} is initialized more than one time.
 *
 * @author schuettec
 *
 */
public class AlreadyInitializedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public AlreadyInitializedException() {
    super();
  }

  public AlreadyInitializedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public AlreadyInitializedException(String message, Throwable cause) {
    super(message, cause);
  }

  public AlreadyInitializedException(String message) {
    super(message);
  }

  public AlreadyInitializedException(Throwable cause) {
    super(cause);
  }

}
