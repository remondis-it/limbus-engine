package com.remondis.limbus.monitoring;

/**
 * This interface defines the Limbus Monitoring Facade activator. It is used to initialize the monitoring with a valid
 * configuration.
 *
 * @author schuettec
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
