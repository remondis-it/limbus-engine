package com.remondis.limbus.monitoring.publisher;

import com.remondis.limbus.monitoring.CallImmediately;
import com.remondis.limbus.monitoring.Publisher;

@Publisher
public interface SomeOtherPublisher {

  public void someOtherMethod();

  @CallImmediately
  public void callImmediatelyMethod();

}
