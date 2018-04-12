package com.remondis.limbus.monitoring.dummies;

import com.remondis.limbus.monitoring.Publisher;

@Publisher
public interface InvalidBecauseHasObjectReturnTypes {

  public void publish(String test);

  public String publishInvalid(String test);

}
