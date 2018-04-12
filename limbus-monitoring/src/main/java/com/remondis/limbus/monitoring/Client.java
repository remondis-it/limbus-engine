package com.remondis.limbus.monitoring;

import static com.remondis.limbus.monitoring.ProxyUtils.createRecordCallProxy;
import static com.remondis.limbus.monitoring.ProxyUtils.noopProxy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The client is the implementation of the {@link Monitoring} interface and is
 * the object that will be passed to classes that request a monitoring.
 *
 * @author schuettec
 *
 */
class Client implements Monitoring {

  private ConcurrentHashMap<Class<?>, Object> cache = new ConcurrentHashMap<>();
  private String name;

  public Client(String name) {
    this.name = name;
  }

  @Override
  public <P> P publish(Class<P> publisher) {
    if (MonitoringFactory.perform()) {
      return cachedPublisher(publisher);
    } else {
      return noopProxy(publisher);
    }
  }

  protected <P> P cachedPublisher(Class<P> publisher) {
    Object object = cache.get(publisher);
    if (object == null) {
      synchronized (cache) {
        object = createRecordCallProxy(name, publisher);
        cache.put(publisher, object);
      }
    }
    return publisher.cast(object);
  }

}
