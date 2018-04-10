package org.max5.limbus;

/**
 * This interface defines an action that is performed with a specific thread context class loader.
 *
 * @param <R>
 *        The return value type
 * @param <E>
 *        The type of exception that may be thrown.
 * @author schuettec
 *
 */
public interface LimbusContextAction<R, E extends Throwable> {

  public R doAction() throws E;

}
