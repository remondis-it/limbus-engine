package com.remondis.limbus_integrations;

import java.util.UUID;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.api.LimbusPlugin;

public class TestPlugin extends Initializable<Exception> implements LimbusPlugin {

  @Override
  protected void performInitialize() throws Exception {
  }

  public String anonymousMethod() {
    return UUID.randomUUID()
        .toString();
  }

  @Override
  protected void performFinish() {
  }

}
