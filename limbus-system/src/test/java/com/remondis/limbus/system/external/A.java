package com.remondis.limbus.system.external;

import com.remondis.limbus.Initializable;

public class A extends Initializable<Exception> {
  public static A instance;

  public A() {
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