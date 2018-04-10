package org.max5.limbus.system.external;

import org.max5.limbus.Initializable;

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