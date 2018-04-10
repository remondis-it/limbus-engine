package org.max5.limbus;

/**
 * This interface defines objects that have to be initialized before calling further operations.
 *
 * @param <E>
 *        The type of the business exception the implementation may throw during initialization.
 * @author schuettec
 *
 */
public interface IInitializable<E extends Exception> {

  /**
   * Initializes this object. This method is thread safe.
   *
   * @throws Exception
   *         Thrown if the initialization failed.
   */
  public void initialize() throws E;

  /**
   * This method deinitializes this object.
   */
  public void finish();
}
