package org.integrationtest;

public class ConsolePublisherImpl implements ConsolePublisher {

  @Override
  public void console(String message) {
    System.out.println(message);
  }

  @Override
  public void console(String format, Object... args) {
    System.out.printf(format, args);
  }

}
