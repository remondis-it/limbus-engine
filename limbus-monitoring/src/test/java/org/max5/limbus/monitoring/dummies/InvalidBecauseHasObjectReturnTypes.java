package org.max5.limbus.monitoring.dummies;

import org.max5.limbus.monitoring.Publisher;

@Publisher
public interface InvalidBecauseHasObjectReturnTypes {

  public void publish(String test);

  public String publishInvalid(String test);

}
