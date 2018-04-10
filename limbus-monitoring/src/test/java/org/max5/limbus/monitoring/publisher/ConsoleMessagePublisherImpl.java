package org.max5.limbus.monitoring.publisher;

public class ConsoleMessagePublisherImpl extends AbstractMessagePublisher {

  @Override
  public void message(String message) {
    super.message(message);
    System.out.println(message);
  }

  @Override
  public void message(String format, Object... params) {
    super.message(format, params);
    System.out.println(String.format(format, params));
  }

}
