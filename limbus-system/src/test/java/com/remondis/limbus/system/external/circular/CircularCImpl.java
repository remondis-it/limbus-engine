package com.remondis.limbus.system.external.circular;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.system.api.LimbusComponent;
import com.remondis.limbus.utils.Lang;

public class CircularCImpl extends Initializable<Exception> implements CircularC {

  @LimbusComponent
  private CircularA a;

  @Override
  public String weNeedA() {
    return a.weNeedD();
  }

  @Override
  protected void performInitialize() throws Exception {
    Lang.denyNull("a", a);
  }

  @Override
  protected void performFinish() {
  }

}
