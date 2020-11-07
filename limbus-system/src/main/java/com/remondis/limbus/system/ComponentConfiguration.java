package com.remondis.limbus.system;

import com.remondis.limbus.api.IInitializable;

public interface ComponentConfiguration {

  /**
   * @return the requestType
   */
  Class<? extends IInitializable<?>> getRequestType();

  /**
   * @return the componentType
   */
  Class<? extends IInitializable<?>> getComponentType();

  /**
   * @return Returns the fail on error flag.
   */
  boolean isFailOnError();

  /**
   * @param failOnError
   *        Sets the fail on error flag.
   */
  void setFailOnError(boolean failOnError);

  /**
   * @return Returns <code>true</code> if this component was configured to be public accessible. Otherwise
   *         <code>false</code>
   */
  boolean isPublicComponent();

}