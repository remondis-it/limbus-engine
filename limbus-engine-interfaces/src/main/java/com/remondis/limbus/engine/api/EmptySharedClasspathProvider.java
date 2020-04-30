package com.remondis.limbus.engine.api;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.api.LimbusClasspathException;

public class EmptySharedClasspathProvider extends Initializable<Exception> implements SharedClasspathProvider {

  @Override
  public Classpath getSharedClasspath() {
    return Classpath.create();
  }

  @Override
  public void checkClasspath() throws LimbusClasspathException {
  }

  @Override
  protected void performInitialize() throws Exception {
  }

  @Override
  protected void performFinish() {
  }

}
