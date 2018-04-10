package org.max5.limbus.monitoring.dummies;

import org.max5.limbus.monitoring.CallImmediately;
import org.max5.limbus.monitoring.Publisher;

@Publisher
public interface DummyPublisher {

  public void publish(String test);

  @CallImmediately
  public void callImmediatelyTest();

}
