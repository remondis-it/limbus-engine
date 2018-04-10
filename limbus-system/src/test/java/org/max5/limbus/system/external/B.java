package org.max5.limbus.system.external;

import org.max5.limbus.Initializable;

public class B extends Initializable<Exception> {
  public static B instance;

  public B() {
    super();
    instance = this;
  }

  @Override
  protected void performInitialize() throws Exception {

  }

  @Override
  protected void performFinish() {

  }

}