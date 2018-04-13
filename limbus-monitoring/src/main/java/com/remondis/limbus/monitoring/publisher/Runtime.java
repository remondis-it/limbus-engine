package com.remondis.limbus.monitoring.publisher;

import com.remondis.limbus.monitoring.CallImmediately;
import com.remondis.limbus.monitoring.Publisher;

/**
 * This publisher interface defines methods to publish the runtime of a certain method. The measurement of the runtime
 * must be performed by the client.
 *
 * @author schuettec
 *
 */
@Publisher
public interface Runtime {
  /**
   * Starts the measurement of a runtime.
   */
  @CallImmediately
  public void start();

  /**
   * Stops the measurement of a runtime and publishes all collected information to the monitoring system.
   */
  @CallImmediately
  public void stop();

  /**
   * Publishes the measured runtimes between invocations of {@link #start()} and {@link #stop()}.
   */
  public void publish();

  /**
   * Publishes a runtime measured by a {@link StopWatch}.
   *
   * @param timeStamp
   *        The timestamp when this record was generated in milliseconds.
   * @param className
   *        The classname of the measured class
   * @param method
   *        The method name the runtime was measured in
   * @param lineNumberStart
   *        (Optional) The line number where the runtime measurement was started
   * @param lineNumberEnd
   *        (Optional) The line number where the runtime measurement was stopped
   * @param runtime
   *        The runtime that was measured in milliseconds.
   */
  public void publishRuntime(long timeStamp, String className, String method, Integer lineNumberStart,
      Integer lineNumberEnd, long runtime);
}
