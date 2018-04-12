package com.remondis.limbus.monitoring;

/**
 * This object allows clients to publish monitoring events using a specified publisher interface. The monitoring events
 * are processes asynchronously by the monitoring framework. If the monitoring was not configured at runtime, the
 * methods result in a no-op.
 *
 * @author schuettec
 *
 */
public interface Monitoring {

  /**
   * Returns a publisher object to push monitoring events to.
   *
   * @param publisher
   *        The publisher interface to be used.
   * @return Returns a publisher object to push monitoring events to.
   */
  public <P> P publish(Class<P> publisher);

}
