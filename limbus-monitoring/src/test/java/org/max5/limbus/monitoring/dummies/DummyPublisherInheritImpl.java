package org.max5.limbus.monitoring.dummies;

import java.util.LinkedList;
import java.util.List;

public class DummyPublisherInheritImpl extends DummyPublisherImpl implements AnotherPublisher {

  private List<String> anotherStrings = new LinkedList<String>();

  public DummyPublisherInheritImpl() {
  }

  @Override
  public void publish(String test) {

  }

  @Override
  public void anotherPublish(String test) {
    anotherStrings.add(test);
  }

  /**
   * @return the anotherStrings
   */
  public List<String> getAnotherStrings() {
    return anotherStrings;
  }

}
