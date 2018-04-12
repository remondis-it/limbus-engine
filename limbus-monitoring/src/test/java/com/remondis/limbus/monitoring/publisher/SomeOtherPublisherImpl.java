package com.remondis.limbus.monitoring.publisher;

import java.util.concurrent.atomic.AtomicInteger;

public class SomeOtherPublisherImpl implements SomeOtherPublisher {

  protected String threadNameCallImmediately;
  protected String threadNameSomeOther;

  protected transient AtomicInteger calls;

  protected Object readResolve() {
    calls = new AtomicInteger();
    return this;
  }

  @Override
  public void someOtherMethod() {
    calls.incrementAndGet();
    this.threadNameSomeOther = Thread.currentThread()
        .getName();
  }

  public int callCount() {
    return calls.get();
  }

  @Override
  public void callImmediatelyMethod() {
    this.threadNameCallImmediately = Thread.currentThread()
        .getName();
  }

  public String getThreadNameCallImmediately() {
    return threadNameCallImmediately;
  }

  public String getThreadNameSomeOther() {
    return threadNameSomeOther;
  }

}
