package com.remondis.limbus;

import com.remondis.limbus.engine.LimbusEngineImpl;
import com.remondis.limbus.engine.LimbusUtil;

public class FailOnInitializeEngine extends LimbusEngineImpl {

  public FailOnInitializeEngine() {
  }

  @Override
  protected void performInitialize() throws Exception {
    super.performInitialize();

    throw new Exception("Thrown by test case for test purposes - ignore this exception.");
  }

  @Override
  protected String[] getPublicAccessPackages() {
    return new String[] {
        LimbusUtil.PUBLIC_LIMBUS_API_PACKAGE_PREFIX
    };
  }

}
