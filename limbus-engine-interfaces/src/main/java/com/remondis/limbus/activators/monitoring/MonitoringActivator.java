package com.remondis.limbus.activators.monitoring;

/**
 * This interface defines the Limbus Monitoring Facade activator. It is used to initialize the monitoring with a valid
 * configuration.
 *
 * 
 *
 */
public interface MonitoringActivator {

  /**
   * Initializes the monitoring and if not successfull defaults to a No-Op state.
   */
  public void initializeMonitoring();

  /**
   * Finishes the monitoring and frees all resources.
   */
  public void finishMonitoring();

}
