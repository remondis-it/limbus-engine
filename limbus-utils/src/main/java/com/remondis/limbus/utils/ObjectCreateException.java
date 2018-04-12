package com.remondis.limbus.utils;

/**
 * This exception can be thrown by the {@link ReflectionUtil} if the creation of an object fails.
 *
 * @author schuettec
 *
 */
public class ObjectCreateException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public ObjectCreateException() {
  }

  public ObjectCreateException(String message) {
    super(message);
  }

  public ObjectCreateException(Throwable cause) {
    super(cause);
  }

  public ObjectCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public ObjectCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
