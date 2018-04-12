package com.remondis.limbus.system.external;

public class FailingProducerImpl implements Producer {

  @Override
  public void initialize() throws RuntimeException {
    throw new RuntimeException("This exception is thrown for test purposes - ignore it!");
  }

  @Override
  public void finish() {
  }

  @Override
  public String getMessage() {
    return null;
  }

}
