package com.remondis.limbus.engine.actions;

/**
 * This interface defines the needed information from an action to be executable per reflection.
 *
 * 
 *
 */
public interface ActionExecution {

  /**
   * @return the name of the class
   */
  public String getClassname();

  /**
   * @return the name of the method to execute
   */
  public String getMethodname();

  /**
   * @return the name of the type the method returns
   */
  public String getReturnType();

  /**
   * @return the expected types from method signature
   */
  public String[] getMethodSignatureTypes();

  /**
   * @return the parameters to call the method with
   */
  public Object[] getParameters();
}
