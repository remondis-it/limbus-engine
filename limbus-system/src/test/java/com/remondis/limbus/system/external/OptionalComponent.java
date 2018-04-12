package com.remondis.limbus.system.external;

import com.remondis.limbus.Initializable;

public class OptionalComponent extends Initializable<Exception> {

  @Override
  protected void performInitialize() throws Exception {
    throw new Exception("This exception is thrown for test purposes - ignore it!");
  }

  @Override
  protected void performFinish() {
  }

}
