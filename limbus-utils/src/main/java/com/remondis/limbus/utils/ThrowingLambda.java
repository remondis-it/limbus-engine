package com.remondis.limbus.utils;

@FunctionalInterface
public interface ThrowingLambda {
  public void doIt() throws LambdaException;

}
