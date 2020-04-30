package com.remondis.limbus.activators.logging;

public class LoggingActivatorException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LoggingActivatorException() {
  }

  public LoggingActivatorException(String message) {
    super(message);
  }

  public LoggingActivatorException(Throwable cause) {
    super(cause);
  }

  public LoggingActivatorException(String message, Throwable cause) {
    super(message, cause);
  }

  public LoggingActivatorException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
