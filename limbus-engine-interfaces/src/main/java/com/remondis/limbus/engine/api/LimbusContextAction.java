package com.remondis.limbus.engine.api;

/**
 * This interface defines an action that is performed with a specific thread context class loader.
 *
 * @param <R>
 *        The return value type
 * @param <E>
 *        The type of exception that may be thrown.
 * 
 *
 */
public interface LimbusContextAction<R, E extends Throwable> {

  public R doAction() throws E;

}
