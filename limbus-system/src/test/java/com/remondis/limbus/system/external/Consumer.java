package com.remondis.limbus.system.external;

import com.remondis.limbus.IInitializable;

/**
 * Service definition consumer
 *
 * @author schuettec
 *
 */
public interface Consumer extends IInitializable<RuntimeException> {
  public String consumeMessage();
}
