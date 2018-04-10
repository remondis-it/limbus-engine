package org.max5.limbus.monitoring.publisher;

import org.max5.limbus.monitoring.Publisher;

@Publisher
public interface MessagePublisher {

  public void message(String message);

  public void message(String format, Object... params);

}
