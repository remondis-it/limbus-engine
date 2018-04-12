package com.remondis.limbus.system.external;

import com.remondis.limbus.Initializable;

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