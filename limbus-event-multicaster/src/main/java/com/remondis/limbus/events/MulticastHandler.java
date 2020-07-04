package com.remondis.limbus.events;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.utils.ReflectionUtil;

/**
 * Implementation of an {@link EventMulticaster} that uses {@link Proxy}-classes to multicast event calls to
 * subscribers.
 * <p>
 * <b>Note: The {@link EventMulticaster} can be configured to act in to different ways:
 * <ul>
 * <li>If <code>throwException</code>
 * is <code>false</code> and a subscriber throws an exception this only creates a warning in the
 * logs.</li>
 * <li>If <code>throwException</code> is <code>true</code></li> a potential exception will be re-thrown by the multicast
 * event method. The processing of further subscribers is then aborted and not all subscribers may be notified.</li>
 * </p>
 *
 * 
 *
 * @param <I>
 */
final class MulticastHandler<I> implements EventMulticaster<I> {

  private static final Logger log = LoggerFactory.getLogger(MulticastHandler.class);

  private ConcurrentLinkedQueue<I> subscribers;

  private I localProxy;
  private I silentLocalProxy;

  /**
   * Holds the handler that re-throws exceptions from subscribers
   */
  private InvocationHandler throwingInvocationHandler = new InvocationHandler() {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Iterator<I> it = subscribers.iterator();
      while (it.hasNext()) {
        I subscriber = it.next();
        try {
          method.invoke(subscriber, args);
        } catch (InvocationTargetException e) {
          // Translate to the cause of the invocation target exception because thats the business logic exception.
          throw e.getCause();
        } catch (Exception e) {
          String message = String.format("Cannot multicast event to subscriber of type %s", subscriber.getClass()
              .getName());
          throw new Exception(message, e);
        }
      }
      return null;
    }
  };

  /**
   * Holds the invocation handler that supresses exceptions from subscribers
   */
  private InvocationHandler silentInvocationHandler = new InvocationHandler() {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Iterator<I> it = subscribers.iterator();
      while (it.hasNext()) {
        I subscriber = it.next();

        try {
          method.invoke(subscriber, args);
        } catch (InvocationTargetException e) {
          // Skip exceptions thrown from the implementation.
          // Translate to the cause of the invocation target exception because thats the business logic exception.
          // Be silent but log exceptions due to implementation faults.
          logInvocationError(subscriber, e.getCause());
        } catch (Exception e) {
          // Be silent but log exceptions due to implementation faults.
          logInvocationError(subscriber, e);
        }
      }
      return null;
    }

    private void logInvocationError(I subscriber, Throwable e) {
      log.debug(String.format("Cannot multicast event to subscriber of type %s", subscriber.getClass()
          .getName()), e);
    }
  };

  MulticastHandler(Class<I> subscriberInterface) {
    this.subscribers = new ConcurrentLinkedQueue<I>();
    this.localProxy = createMulticasterProxy(subscriberInterface, throwingInvocationHandler);
    this.silentLocalProxy = createMulticasterProxy(subscriberInterface, silentInvocationHandler);
  }

  @Override
  public I multicast() {
    return localProxy;
  }

  @Override
  public I multicastSilently() {
    return silentLocalProxy;
  }

  @Override
  public void addSubscriber(I subscriber) {
    subscribers.add(subscriber);
  }

  @Override
  public void addAllSubscribers(I[] subscribers) {
    this.subscribers.addAll(Arrays.asList(subscribers));
  }

  @Override
  public void removeSubscriber(I subscriber) {
    subscribers.remove(subscriber);
  }

  @Override
  public void clear() {
    subscribers.clear();
  }

  @SuppressWarnings("unchecked")
  protected I createMulticasterProxy(Class<I> subscriberInterface, InvocationHandler handler) {
    ClassLoader classLoader = ReflectionUtil.getClassLoader(getClass());
    Object proxy = Proxy.newProxyInstance(classLoader, new Class[] {
        subscriberInterface
    }, handler);

    return (I) proxy;
  }

  @Override
  public Set<I> getSubscribers() {
    return new HashSet<I>(subscribers);
  }
}
