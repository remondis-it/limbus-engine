package com.remondis.limbus.utils;

/**
 * Thrown if a system configuration cannot be read, written or is otherwise invalid or corrupted.
 *
 * 
 *
 */
public class SerializeException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public SerializeException() {
    super();
  }

  public SerializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public SerializeException(String message, Throwable cause) {
    super(message, cause);
  }

  public SerializeException(String message) {
    super(message);
  }

  public SerializeException(Throwable cause) {
    super(cause);
  }

}
