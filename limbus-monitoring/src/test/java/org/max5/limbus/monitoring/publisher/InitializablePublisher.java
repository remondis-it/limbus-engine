package org.max5.limbus.monitoring.publisher;

import org.max5.limbus.monitoring.Publisher;

@Publisher
public interface InitializablePublisher {
  public void reset();
}
