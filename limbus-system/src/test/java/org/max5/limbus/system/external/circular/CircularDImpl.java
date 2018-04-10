package org.max5.limbus.system.external.circular;

import org.max5.limbus.Initializable;

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
