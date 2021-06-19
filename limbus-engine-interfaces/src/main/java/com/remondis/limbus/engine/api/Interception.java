package com.remondis.limbus.engine.api;

/**
 * An object representing an intercepted method invocation.
 */
public interface Interception {

  public Object proceed() throws Throwable;

}
