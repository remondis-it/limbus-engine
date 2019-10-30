package com.remondis.limbus.system.external;

import com.remondis.limbus.api.IInitializable;

/**
 * Service definition consumer
 *
 * @author schuettec
 *
 */
public interface Consumer extends IInitializable<RuntimeException> {
  public String consumeMessage();
}
