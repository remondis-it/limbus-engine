package org.max5.limbus.system.external;

import org.max5.limbus.IInitializable;

/**
 * Service definition consumer
 *
 * @author schuettec
 *
 */
public interface Consumer extends IInitializable<RuntimeException> {
  public String consumeMessage();
}
