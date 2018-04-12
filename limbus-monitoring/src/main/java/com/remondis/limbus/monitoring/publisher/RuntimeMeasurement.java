package com.remondis.limbus.monitoring.publisher;

import com.remondis.limbus.monitoring.ClientContext;

class RuntimeMeasurement {
  private ClientContext clientContextOnStart;
  private ClientContext clientContextOnEnd;
  private Long durationInMilliseconds;

  public RuntimeMeasurement(ClientContext clientContextOnStart, ClientContext clientContextOnEnd,
      Long durationInMilliseconds) {
    super();
    this.clientContextOnStart = clientContextOnStart;
    this.clientContextOnEnd = clientContextOnEnd;
    this.durationInMilliseconds = durationInMilliseconds;
  }

  /**
   * @return the clientContextOnStart
   */
  protected ClientContext getClientContextOnStart() {
    return clientContextOnStart;
  }

  /**
   * @return the clientContextOnEnd
   */
  protected ClientContext getClientContextOnEnd() {
    return clientContextOnEnd;
  }

  /**
   * @return the durationInMilliseconds
   */
  protected Long getDurationInMilliseconds() {
    return durationInMilliseconds;
  }

}
