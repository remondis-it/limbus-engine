package com.remondis.limbus.engine.api;

/**
 * Result of a reflective method invocation.
 */
public class InvocationResult {

  private Object returnValue;
  private boolean hasReturnType;

  private InvocationResult() {

  }

  private InvocationResult(Object returnValue, boolean hasReturnType) {
    super();
    this.returnValue = returnValue;
    this.hasReturnType = hasReturnType;
  }

  /**
   * Creates a new {@link InvocationResult} for a method that returns a value.
   * 
   * @param value Value or <code>null</code> if the method returned <code>null</code>.
   * @return Returns a new instance.
   */
  public static InvocationResult returnValue(Object value) {
    return new InvocationResult(value, true);
  }

  /**
   * @return Returns a new {@link InvocationResult} for a method without return type.
   */
  public static InvocationResult noReturn() {
    return new InvocationResult(null, false);
  }

  public boolean methodReturnsValues() {
    return hasReturnType;
  }

  public Object getReturnValue() {
    if (!hasReturnType) {
      throw new IllegalStateException("The method does not provide a return type.");
    } else {
      return returnValue;
    }
  }

}
