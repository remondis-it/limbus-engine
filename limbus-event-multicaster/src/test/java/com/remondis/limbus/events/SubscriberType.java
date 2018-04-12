package com.remondis.limbus.events;

public interface SubscriberType {

  public void notified() throws Exception;

  public void noException();

}
