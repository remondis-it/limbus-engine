package com.remondis.limbus.staging;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.system.api.LimbusComponent;

@LimbusComponent
public interface MockComponent extends IInitializable<RuntimeException> {

  public String sayHello();

}
