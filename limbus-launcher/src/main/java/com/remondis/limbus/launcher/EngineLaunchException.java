package com.remondis.limbus.launcher;

public class EngineLaunchException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public EngineLaunchException() {
  }

  public EngineLaunchException(String message) {
    super(message);
  }

  public EngineLaunchException(Throwable cause) {
    super(cause);
  }

  public EngineLaunchException(String message, Throwable cause) {
    super(message, cause);
  }

  public EngineLaunchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
