package com.remondis.limbus.system;

/**
 * This exception is thrown by {@link LimbusSystem} if the initialization of the Limbus system components failed.
 *
 * @author schuettec
 *
 */
public class LimbusSystemException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  LimbusSystemException() {
    super();
  }

  LimbusSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  LimbusSystemException(String message, Throwable cause) {
    super(message, cause);
  }

  LimbusSystemException(String message) {
    super(message);
  }

  LimbusSystemException(Throwable cause) {
    super(cause);
  }

}
