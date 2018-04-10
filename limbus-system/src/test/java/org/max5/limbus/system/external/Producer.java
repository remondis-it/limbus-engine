package org.max5.limbus.system.external;

import org.max5.limbus.IInitializable;

/**
 * Service definition producer
 *
 * @author schuettec
 *
 */
public interface Producer extends IInitializable<RuntimeException> {
  public String getMessage();
}
