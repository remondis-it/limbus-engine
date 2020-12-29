package com.remondis.limbus.events;

import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * Factory to create {@link EventMulticaster} instances.
 *
 * @author schuettec
 *
 */
public class EventMulticasterFactory {

  /**
   * Creates an {@link EventMulticaster} for the specified subscriber type. The {@link EventMulticaster} created will
   * forward thrown exceptions from subscribers. <b>Note: An exception aborts the notifaction of further
   * subscribers.</b>
   *
   * @param subscriberType
   *        The subscriber interface.
   *
   * @return Returns the {@link EventMulticaster}.
   */
  public static <I> EventMulticaster<I> create(Class<I> subscriberType) {
    Lang.denyNull("subscriber type", subscriberType);
    ReflectionUtil.denyNotInterface(subscriberType);
    MulticastHandler<I> handler = new MulticastHandler<I>(subscriberType);
    return handler;
  }

  /**
   * Creates an <b>asynchronous</b> {@link EventMulticaster} for the specified subscriber type. The
   * {@link EventMulticaster} created will forward thrown exceptions from subscribers. <b>Note: An exception aborts the
   * notifaction of further subscribers.</b>
   * 
   * <p>
   * The subscribers will be notified asynchronously in a separate thread.
   * </p>
   *
   * @param subscriberType
   *        The subscriber interface.
   *
   * @return Returns the {@link EventMulticaster}.
   */
  public static <I> EventMulticaster<I> createAsync(Class<I> subscriberType) {
    Lang.denyNull("subscriber type", subscriberType);
    ReflectionUtil.denyNotInterface(subscriberType);
    AsyncMulticastHandler<I> handler = new AsyncMulticastHandler<I>(subscriberType);
    return handler;
  }

}
