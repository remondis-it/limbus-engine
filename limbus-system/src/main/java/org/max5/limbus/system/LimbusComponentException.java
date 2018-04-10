package org.max5.limbus.system;

/**
 * Thrown if a Limbus component managed by a {@link LimbusSystem} cannot be created, the injection of dependencies
 * failed or the initializing fails.
 *
 * @author schuettec
 *
 */
public class LimbusComponentException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  LimbusComponentException() {
    super();
  }

  LimbusComponentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  LimbusComponentException(String message, Throwable cause) {
    super(message, cause);
  }

  LimbusComponentException(String message) {
    super(message);
  }

  LimbusComponentException(Throwable cause) {
    super(cause);
  }

}
