package org.max5.limbus.system;

import java.lang.reflect.Proxy;

import org.max5.limbus.IInitializable;

/**
 * This object factory behaves like {@link ReflectiveObjectFactory} but creates intercepting proxy objects for public
 * components. Those proxy objects determine the runtime of each method a public component has. The runtime information
 * will be published using the Limbus Monitoring framework.
 *
 * @author schuettec
 *
 */
public class MonitoringObjectFactory extends ReflectiveObjectFactory {
  /**
   * Called by {@link LimbusSystem} to create a reference to the specified public component. Gives object factories the
   * chance to create a proxy object.
   *
   * @param requestType
   *        The request type of the public component
   * @param componentType
   *        The component implementation type
   * @param instance
   *        The original instance of the component
   * @return Returns an object that is used to access the component instance. Implementations may return proxy objects
   *         here.
   */
  @Override
  public IInitializable<?> createPublicReference(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> componentType, IInitializable<?> instance) {

    Class<?>[] interfaces = null;

    if (instance instanceof LimbusSystemListener) {
      interfaces = new Class<?>[] {
          IInitializable.class, requestType, LimbusSystemListener.class
      };
    } else {
      interfaces = new Class<?>[] {
          IInitializable.class, requestType
      };
    }

    Object monitoringProxy = Proxy.newProxyInstance(requestType.getClassLoader(), interfaces,
        new MonitoringProxy(instance));

    return IInitializable.class.cast(monitoringProxy);
  }

}
