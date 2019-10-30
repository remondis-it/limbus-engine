package com.remondis.limbus.staging;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.system.LimbusComponent;

@LimbusComponent
public class MockComponentImpl extends Initializable<RuntimeException> implements MockComponent {

  public static final String HELLO_WORLD = "Hello World!";

  @Override
  public String sayHello() {
    return HELLO_WORLD;
  }

}
