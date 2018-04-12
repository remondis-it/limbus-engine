package com.remondis.limbus.monitoring.publisher;

import com.remondis.limbus.monitoring.Publisher;

@Publisher
public interface MessagePublisher {

  public void message(String message);

  public void message(String format, Object... params);

}
