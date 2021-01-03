package com.remondis.limbus.system;

/**
 * Thrown by {@link LimbusSystem} if a component was requested that does not exist in the system.
 *
 * @author schuettec
 *
 */
public class SingleComponentExpectedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public SingleComponentExpectedException() {
  }

  public SingleComponentExpectedException(String message) {
    super(message);
  }

  public SingleComponentExpectedException(Throwable cause) {
    super(cause);
  }

  public SingleComponentExpectedException(String message, Throwable cause) {
    super(message, cause);
  }

  public SingleComponentExpectedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * Creates a new {@link SingleComponentExpectedException} for the case that a single component was expected but
   * multiple components are available.
   * 
   * @param requestType
   *        The type that was used to request the component.
   */
  public static SingleComponentExpectedException moreThanOneComponentAvailable(Class<?> requestType) {
    return new SingleComponentExpectedException(
        String.format("A single component was requested, but multiple candidates are available for request type: %s",
            requestType.getName()));
  }

}
