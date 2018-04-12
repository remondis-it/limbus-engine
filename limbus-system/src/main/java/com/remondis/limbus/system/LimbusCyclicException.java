package com.remondis.limbus.system;

/**
 * Thrown by the {@link LimbusSystem} if a circular dependency request was detected on the object graph.
 * 
 * @author schuettec
 *
 */
public class LimbusCyclicException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  LimbusCyclicException() {
    super();
  }

  LimbusCyclicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  LimbusCyclicException(String message, Throwable cause) {
    super(message, cause);
  }

  LimbusCyclicException(String message) {
    super(message);
  }

  LimbusCyclicException(Throwable cause) {
    super(cause);
  }

}
