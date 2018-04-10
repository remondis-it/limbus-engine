package org.integrationtest;

import org.max5.limbus.monitoring.Publisher;

@Publisher
public interface ConsolePublisher {

  public void console(String message);

  public void console(String format, Object... params);

}
