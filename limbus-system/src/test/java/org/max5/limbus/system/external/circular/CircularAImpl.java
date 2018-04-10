package org.max5.limbus.system.external.circular;

import org.max5.limbus.Initializable;
import org.max5.limbus.system.LimbusComponent;
import org.max5.limbus.utils.Lang;

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
