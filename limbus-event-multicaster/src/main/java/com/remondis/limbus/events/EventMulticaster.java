package com.remondis.limbus.events;

import java.util.Set;

/**
 * This event multicaster manages a list of subscribers that are called on an income of a specific event. The event
 * multicaster is assumed to be thread safe.
 *
 * @param <I>
 *        The subscriber interface type.
 * @author schuettec
 *
 */
public interface EventMulticaster<I> {

  /**
   * @param subscribers
   *        Adds all subscribers to this {@link EventMulticaster}.
   */
  public void addAllSubscribers(I[] subscribers);

  /**
   * @param subscriber
   *        Adds a subscriber to this {@link EventMulticaster}.
   */
  public void addSubscriber(I subscriber);

  /**
   * @param subscriber
   *        Removes a subscriber from this {@link EventMulticaster}.
   */
  public void removeSubscriber(I subscriber);

  /**
   * @return Returns all registered subscribers.
   */
  public Set<I> getSubscribers();

  /**
   * Removes all registered subscribers.
   */
  public void clear();

  /**
   * Returns the multicast object to perform a subscriber call. Multicasts performed on this multicast object will
   * re-throw exceptions from the subscriber. <b>Therefore it is not guaranteed, that all subscribers are notified.</b>
   *
   * @return Returns the multicast object.
   */
  public I multicast();

  /**
   * Returns the multicast object to perform a subscriber call. Multicasts performed on this multicast object will
   * <b>suppress</b> exceptions from the subscribers. <b>A silent multicast will ensure that all subscribers are
   * notified.</b>
   *
   * @return Returns the multicast object.
   */
  public I multicastSilently();

}
