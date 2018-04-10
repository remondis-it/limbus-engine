package org.max5.limbus.system.external;

import org.max5.limbus.IInitializable;

/**
 * Service definition aggregator
 *
 * @author schuettec
 *
 */
public interface Aggregator extends IInitializable<RuntimeException> {
  public void doSomething();

  public String getMessage();
}
