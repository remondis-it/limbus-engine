package com.remondis.limbus.monitoring.publisher;

public class DecoratedConsoleMessagePublisherImpl extends ConsoleMessagePublisherImpl {

  @Override
  public void message(String message) {
    super.message("Decorated:" + message);
  }

  @Override
  public void message(String format, Object... params) {
    super.message("Decorated:" + format, params);
  }

}
