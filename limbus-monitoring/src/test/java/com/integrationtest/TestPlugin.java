package com.integrationtest;

import com.remondis.limbus.api.LimbusPlugin;

public interface TestPlugin extends LimbusPlugin {

  /**
   * Called to trigger the use of a monitoring object.
   */
  public void callMonitoring();

  /**
   * When called, the plugin should hold the reference to the monitoring object.
   */
  public void holdPublisher();

  /**
   * When called, the plugin should release the reference to the monitoring object.
   */
  public void releasePublisher();

}
