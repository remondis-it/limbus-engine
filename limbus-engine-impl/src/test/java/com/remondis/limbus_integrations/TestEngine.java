package com.remondis.limbus_integrations;

import com.remondis.limbus.engine.LimbusEngineImpl;

public class TestEngine extends LimbusEngineImpl {

  @Override
  protected String[] getPublicAccessPackages() {
    return new String[] {
        TestPlugin.class.getPackage()
            .getName()
    };
  }

}
