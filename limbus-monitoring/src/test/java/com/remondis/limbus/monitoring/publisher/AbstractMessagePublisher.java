package com.remondis.limbus.monitoring.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractMessagePublisher implements MessagePublisher {

  private LinkedBlockingQueue<String> messageCalls;
  private LinkedBlockingQueue<String> formatMessageCalls;

  public AbstractMessagePublisher() {
    readResolve();
  }

  protected Object readResolve() {
    if (messageCalls == null) {
      messageCalls = new LinkedBlockingQueue<>();
    }
    if (formatMessageCalls == null) {
      formatMessageCalls = new LinkedBlockingQueue<>();
    }
    return this;
  }

  @Override
  public void message(String message) {
    messageCalls.add(message);
  }

  @Override
  public void message(String format, Object... params) {
    String message = String.format(format, params);
    formatMessageCalls.add(message);
  }

  /**
   * @return the messageCalls
   */
  public List<String> getMessageCalls() {
    return new ArrayList<>(messageCalls);
  }

  /**
   * @return the formatMessageCalls
   */
  public List<String> getFormatMessageCalls() {
    return new ArrayList<>(formatMessageCalls);
  }

}
