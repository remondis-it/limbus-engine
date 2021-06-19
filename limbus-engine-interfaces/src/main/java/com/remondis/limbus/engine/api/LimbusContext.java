package com.remondis.limbus.engine.api;

import com.remondis.limbus.api.Classpath;

/**
 * This interface provides a way to perform an action in the plugin's context.
 *
 * @author schuettec
 *
 */
public interface LimbusContext {
  /**
   * @return Returns the classpath of the current context.
   */
  public Classpath getClasspath();

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
