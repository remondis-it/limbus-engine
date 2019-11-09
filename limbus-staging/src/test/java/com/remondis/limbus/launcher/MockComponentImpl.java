package com.remondis.limbus.launcher;

import com.remondis.limbus.Initializable;
import com.remondis.limbus.system.LimbusComponent;

@LimbusComponent
public class MockComponentImpl extends Initializable<RuntimeException> implements MockComponent {

  public static final String HELLO_WORLD = "Hello World!";

  @Override
  public String sayHello() {
    return HELLO_WORLD;
  }

}
