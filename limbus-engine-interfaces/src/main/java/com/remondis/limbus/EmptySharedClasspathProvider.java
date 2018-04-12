package com.remondis.limbus;

import com.remondis.limbus.exceptions.LimbusClasspathException;

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
