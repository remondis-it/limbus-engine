package com.remondis.limbus.launcher;

import com.remondis.limbus.IInitializable;
import com.remondis.limbus.system.LimbusComponent;

@LimbusComponent
public interface MockComponent extends IInitializable<RuntimeException> {

  public String sayHello();

}
