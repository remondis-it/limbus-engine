package com.remondis.limbus.monitoring.dummies;

import com.remondis.limbus.monitoring.CallImmediately;
import com.remondis.limbus.monitoring.Publisher;

@Publisher
public interface DummyPublisher {

  public void publish(String test);

  @CallImmediately
  public void callImmediatelyTest();

}
