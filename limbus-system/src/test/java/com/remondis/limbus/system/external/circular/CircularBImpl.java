package com.remondis.limbus.system.external.circular;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.system.LimbusComponent;
import com.remondis.limbus.utils.Lang;

public class CircularBImpl extends Initializable<Exception> implements CircularB {

  @LimbusComponent
  private CircularC c;

  @Override
  public String weNeedC() {
    return c.weNeedA();
  }

  @Override
  protected void performInitialize() throws Exception {
    Lang.denyNull("c", c);
  }

  @Override
  protected void performFinish() {
  }

}
