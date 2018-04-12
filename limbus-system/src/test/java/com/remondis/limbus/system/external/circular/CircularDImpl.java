package com.remondis.limbus.system.external.circular;

import com.remondis.limbus.Initializable;

public class CircularDImpl extends Initializable<Exception> implements CircularD {

  @Override
  public String weNeedNothing() {
    return "D";
  }

  @Override
  protected void performInitialize() throws Exception {
  }

  @Override
  protected void performFinish() {
  }

}
