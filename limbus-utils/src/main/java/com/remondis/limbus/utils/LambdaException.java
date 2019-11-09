package com.remondis.limbus.utils;

/**
 * This is an implementation of a {@link RuntimeException} that can be used in
 * lambda expressions that do not support throwing {@link Exception}s. All
 * exceptions in the lambda can be wrapped inside the lambda and unwrapped
 * outside the functional structure.
 * 
 */
public class LambdaException extends RuntimeException {

  LambdaException(Exception cause) {
    super(null, cause);
  }

  public static LambdaException of(Exception e) {
    return new LambdaException(e);
  }

  public static void catchFromLambda(ThrowingLambda lambda) throws Exception {
    try {
      lambda.doIt();
    } catch (LambdaException e) {
      throw e.getCause();
    }
  }

  @Override
  public synchronized Exception getCause() {
    return (Exception) super.getCause();
  }

}
