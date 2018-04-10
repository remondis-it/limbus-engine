package org.max5.limbus.system.external.circular;

import org.max5.limbus.Initializable;
import org.max5.limbus.system.LimbusComponent;
import org.max5.limbus.utils.Lang;

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
