package org.max5.limbus.monitoring.dummies;

import java.util.LinkedList;
import java.util.List;

public class DummyPublisherImpl implements DummyPublisher {

  private List<String> publishedStrings = new LinkedList<String>();

  public DummyPublisherImpl() {
  }

  @Override
  public void publish(String test) {
    publishedStrings.add(test);
  }

  /**
   * @return the publishedStrings
   */
  public List<String> getPublishedStrings() {
    return publishedStrings;
  }

  @Override
  public void callImmediatelyTest() {

  }

}
