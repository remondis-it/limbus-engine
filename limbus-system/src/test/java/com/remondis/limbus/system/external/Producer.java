package com.remondis.limbus.system.external;

import com.remondis.limbus.api.IInitializable;

/**
 * Service definition producer
 *
 * @author schuettec
 *
 */
public interface Producer extends IInitializable<RuntimeException> {
  public String getMessage();
}
