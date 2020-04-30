package com.remondis.limbus.engine.security;

public class PermissionCreateException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public PermissionCreateException() {
  }

  public PermissionCreateException(String message) {
    super(message);
  }

  public PermissionCreateException(Throwable cause) {
    super(cause);
  }

  public PermissionCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public PermissionCreateException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
