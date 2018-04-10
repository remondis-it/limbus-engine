package org.max5.limbus.monitoring.publisher;

import org.max5.limbus.monitoring.CallImmediately;
import org.max5.limbus.monitoring.Publisher;

@Publisher
public interface SomeOtherPublisher {

  public void someOtherMethod();

  @CallImmediately
  public void callImmediatelyMethod();

}
