package com.remondis.limbus.monitoring.publisher;

import com.remondis.limbus.monitoring.Publisher;

@Publisher
public interface InitializablePublisher {
  public void reset();
}
