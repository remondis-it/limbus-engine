package com.remondis.limbus.system;

/**
 * Thrown by {@link LimbusSystem} if a component was requested that does not exist in the system.
 *
 * @author schuettec
 *
 */
public class NoSuchComponentException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public NoSuchComponentException() {
  }

  public NoSuchComponentException(String message) {
    super(message);
  }

  public NoSuchComponentException(Throwable cause) {
    super(cause);
  }

  public NoSuchComponentException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoSuchComponentException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * Creates a new exception instance using the request type for the message.
   *
   * @param requestType
   *        The type that was used to request the component.
   */
  public NoSuchComponentException(Class<?> requestType) {
    this(String.format("No component was registered for component type %s", requestType.getName()));
  }

}
