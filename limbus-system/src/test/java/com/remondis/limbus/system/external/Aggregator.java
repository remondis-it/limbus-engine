package com.remondis.limbus.system.external;

import com.remondis.limbus.api.IInitializable;

/**
 * Service definition aggregator
 *
 * 
 *
 */
public interface Aggregator extends IInitializable<RuntimeException> {
  public void doSomething();

  public String getMessage();
}
