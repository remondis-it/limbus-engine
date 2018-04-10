package org.max5.limbus.system.external;

import org.max5.limbus.IInitializable;

/**
 * Service definition filter.
 *
 * @author schuettec
 *
 */
public interface Filter extends IInitializable<RuntimeException> {
  public String filter(String message);
}
