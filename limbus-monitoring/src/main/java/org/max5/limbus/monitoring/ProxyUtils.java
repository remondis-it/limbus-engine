package org.max5.limbus.monitoring;

import static org.max5.limbus.monitoring.Conventions.denyInvalidPublisherInterface;

import java.lang.reflect.Proxy;
import java.util.WeakHashMap;

import org.max5.limbus.utils.ReflectionUtil;

public final class ProxyUtils {

  protected static WeakHashMap<Class<?>, Object> weakNoopProxies = new WeakHashMap<Class<?>, Object>();

  /**
   * Creates a No-Op proxy object for the specified publisher type. The publisher type is assumed to be a valid
   * publisher interface. For the definition of the publisher convention see documentation of {@link Publisher}.
   *
   * @param publisherType
   *        The type of the valid publisher interface.
   * @return Returns the no-op object implementing the specified publisher.
   */
  public static <T> T noopProxy(Class<T> publisherType) {
    denyInvalidPublisherInterface(publisherType);
    Object object = weakNoopProxies.get(publisherType);
    if (object == null) {
      synchronized (weakNoopProxies) {
        T proxy = _createNoopProxy(publisherType);
        weakNoopProxies.put(publisherType, proxy);
        object = proxy;
      }
    }
    return publisherType.cast(object);
  }

  private static <T> T _createNoopProxy(Class<T> publisherType) {
    ClassLoader target = ReflectionUtil.getClassLoader(ProxyUtils.class);
    NoopHandler h = new NoopHandler();
    Object noopProxy = Proxy.newProxyInstance(target, new Class<?>[] {
        publisherType
    }, h);
    return publisherType.cast(noopProxy);
  }

  public static <T> T createRecordCallProxy(String name, Class<T> publisherType) {
    denyInvalidPublisherInterface(publisherType);
    ClassLoader target = ReflectionUtil.getClassLoader(ProxyUtils.class);
    Object noopProxy = Proxy.newProxyInstance(target, new Class<?>[] {
        publisherType
    }, new RecordCallHandler(name, publisherType));
    return publisherType.cast(noopProxy);
  }

}
