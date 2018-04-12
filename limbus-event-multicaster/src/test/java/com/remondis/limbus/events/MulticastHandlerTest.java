package com.remondis.limbus.events;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.remondis.limbus.utils.IllegalTypeException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MulticastHandlerTest {

  @Mock
  protected SubscriberType subscriberMock;

  @Mock
  protected SubscriberType anotherSubscriberMock;

  @Test(expected = IllegalArgumentException.class)
  public void eventMulticasterFactory_01_test() {
    EventMulticasterFactory.create(null);
  }

  @Test(expected = IllegalTypeException.class)
  public void eventMulticasterFactory_02_test() {
    EventMulticasterFactory.create(String.class);
  }

  @Test // Happy path.
  public void multicast_01_test() throws Exception {
    EventMulticaster<SubscriberType> multicaster = EventMulticasterFactory.create(SubscriberType.class);

    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(0)).notified();

    multicaster.addSubscriber(subscriberMock);

    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(1)).notified();
    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(2)).notified();
    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(3)).notified();

    multicaster.removeSubscriber(subscriberMock);
    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(3)).notified();

    multicaster.addSubscriber(subscriberMock);
    multicaster.addSubscriber(anotherSubscriberMock);

    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(4)).notified();
    verify(anotherSubscriberMock, times(1)).notified();
    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(5)).notified();
    verify(anotherSubscriberMock, times(2)).notified();
    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(6)).notified();
    verify(anotherSubscriberMock, times(3)).notified();

    multicaster.removeSubscriber(subscriberMock);
    multicaster.removeSubscriber(anotherSubscriberMock);

    multicaster.multicast()
        .notified();
    verify(subscriberMock, times(6)).notified();
    verify(anotherSubscriberMock, times(3)).notified();

  }

  @Test
  public void multicast_silent_errorhandling_01_test() throws Exception {
    EventMulticaster<SubscriberType> multicaster = EventMulticasterFactory.create(SubscriberType.class);
    multicaster.addSubscriber(subscriberMock);
    multicaster.addSubscriber(anotherSubscriberMock);

    // Let the subscriber throw an exception on notify()
    doThrow(testException()).when(subscriberMock)
        .notified();

    multicaster.multicastSilently()
        .notified();

    verify(subscriberMock, times(1)).notified();
    verify(anotherSubscriberMock, times(1)).notified();

    multicaster.multicastSilently()
        .notified();
    verify(subscriberMock, times(2)).notified();
    verify(anotherSubscriberMock, times(2)).notified();
  }

  private Exception testException() {
    return new Exception("This is a dummy exception and only used for testing - ignore it!");
  }

  @Test
  public void multicast_errorhandling_01_test() throws Exception {
    EventMulticaster<SubscriberType> multicaster = EventMulticasterFactory.create(SubscriberType.class);
    multicaster.addSubscriber(subscriberMock);
    multicaster.addSubscriber(anotherSubscriberMock);

    // Let the subscriber throw an exception on notify()
    doThrow(testException()).when(subscriberMock)
        .notified();

    try {
      multicaster.multicast()
          .notified();
      fail("No exception was thrown.");
    } catch (Exception e) {
    }

    verify(subscriberMock, times(1)).notified();
    verify(anotherSubscriberMock, times(0)).notified();

    try {
      multicaster.multicast()
          .notified();
      fail("No exception was thrown.");
    } catch (Exception e) {
    }
    verify(subscriberMock, times(2)).notified();
    verify(anotherSubscriberMock, times(0)).notified();
  }

}
