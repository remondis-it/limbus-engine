package org.max5.limbus;

import org.max5.limbus.exceptions.LimbusClasspathException;

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
