package org.integrationtest;

import com.remondis.limbus.LimbusEngineImpl;

public class TestEngine extends LimbusEngineImpl {

  @Override
  protected String[] getPublicAccessPackages() {
    return new String[] {
        TestPlugin.class.getPackage()
            .getName()
    };
  }

}
