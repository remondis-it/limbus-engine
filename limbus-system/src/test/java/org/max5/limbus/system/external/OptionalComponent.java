package org.max5.limbus.system.external;

import org.max5.limbus.Initializable;

public class OptionalComponent extends Initializable<Exception> {

  @Override
  protected void performInitialize() throws Exception {
    throw new Exception("This exception is thrown for test purposes - ignore it!");
  }

  @Override
  protected void performFinish() {
  }

}
