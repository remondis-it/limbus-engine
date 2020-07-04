package com.remondis.limbus.api;

/**
 * This interface defines objects that have to be initialized before calling further operations.
 *
 * @param <E>
 *        The type of the business exception the implementation may throw during initialization.
 * 
 *
 */
public interface IInitializable<E extends Exception> {

  /**
   * Initializes this object. This method is thread safe.
   *
   * @throws E
   *         Thrown if the initialization failed.
   */
  public void initialize() throws E;

  /**
   * This method deinitializes this object.
   */
  public void finish();
}
