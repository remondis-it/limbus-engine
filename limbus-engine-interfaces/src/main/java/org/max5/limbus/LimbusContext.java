package org.max5.limbus;

/**
 * This interface provides a way to perform an action in the plugin's context.
 *
 * @author schuettec
 *
 */
public interface LimbusContext {
  /**
   * Performs a context action in the plugin's deploy context.
   *
   * @param callable
   *        The busniess operation.
   * @return Returns the specified value returned by the business operation.
   * @throws E
   *         Thrown on any error.
   */
  public <R, E extends Throwable> R doContextAction(LimbusContextAction<R, E> callable) throws E;
}
