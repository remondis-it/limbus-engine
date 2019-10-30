package com.remondis.limbus.system.external.circular;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.system.LimbusComponent;
import com.remondis.limbus.utils.Lang;

public class CircularAImpl extends Initializable<Exception> implements CircularA {

  @LimbusComponent
  private CircularB b;

  @LimbusComponent
  private CircularD d;

  @Override
  public String weNeedB() {
    return b.weNeedC();
  }

  @Override
  public String weNeedD() {
    return null;
  }

  @Override
  protected void performInitialize() throws Exception {
    Lang.denyNull("b", b);
  }

  @Override
  protected void performFinish() {
  }

}
