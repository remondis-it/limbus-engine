package com.remondis.limbus.engine.api;

import com.remondis.limbus.api.LimbusException;

/**
 * Thrown if the undeploy operation was vetoed and therefore cannot be performed.
 *
 * 
 *
 */
public class UndeployVetoException extends LimbusException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public UndeployVetoException() {
    super();
  }

  public UndeployVetoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public UndeployVetoException(String message, Throwable cause) {
    super(message, cause);
  }

  public UndeployVetoException(String message) {
    super(message);
  }

  public UndeployVetoException(Throwable cause) {
    super(cause);
  }

  public static UndeployVetoException newDefault() {
    return new UndeployVetoException("The undeployment was vetoed!");
  }

}
