package com.remondis.limbus.system.external;

import com.remondis.limbus.api.IInitializable;

/**
 * Service definition filter.
 *
 * @author schuettec
 *
 */
public interface Filter extends IInitializable<RuntimeException> {
  public String filter(String message);
}
