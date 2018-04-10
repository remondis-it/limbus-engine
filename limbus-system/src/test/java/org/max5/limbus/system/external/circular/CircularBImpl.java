package org.max5.limbus.system.external.circular;

import org.max5.limbus.Initializable;
import org.max5.limbus.system.LimbusComponent;
import org.max5.limbus.utils.Lang;

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
