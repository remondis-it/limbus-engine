package com.remondis.limbus.system;

import java.util.HashMap;
import java.util.Map;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.utils.ObjectCreateException;

/**
 * This {@link ObjectFactory} can be configured to be used by the {@link LimbusSystem} to inject mock instances of
 * components.
 *
 * @author schuettec
 *
 */
public class MockObjectFactory implements ObjectFactory {

  protected Map<Class<? extends IInitializable<?>>, IInitializable<?>> publicComponents = new HashMap<>();
  protected Map<Class<? extends IInitializable<?>>, IInitializable<?>> privateComponents = new HashMap<>();

  /**
   * Holds the optional delegation object factory. The delegation factory is used if a component instance is requested
   * that is not known by this mock object factory.
   */
  private ObjectFactory delegateObjectFactory;

  public MockObjectFactory() {

  }

  public MockObjectFactory(ObjectFactory objectFactory) {
    this.delegateObjectFactory = objectFactory;
  }

  @Override
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> implementationType)
      throws ObjectCreateException {
    if (hasPrivateObject(implementationType)) {
      return privateComponents.get(implementationType);
    } else {
      if (hasDelegate()) {
        return delegateObjectFactory.createObject(implementationType);
      } else {
        throw new NoSuchComponentException(implementationType);
      }
    }
  }

  @Override
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> implementationType) throws ObjectCreateException {
    if (hasPublicObject(requestType)) {
      return publicComponents.get(requestType);
    } else {
      if (hasDelegate()) {
        return delegateObjectFactory.createObject(requestType, implementationType);
      } else {
        throw new NoSuchComponentException(requestType);
      }
    }

  }

  public void putPublicComponent(Class<? extends IInitializable<?>> requestType, IInitializable<?> instance) {
    publicComponents.put(requestType, instance);
  }

  public void putPrivateComponent(Class<? extends IInitializable<?>> implementationType, IInitializable<?> instance) {
    privateComponents.put(implementationType, instance);
  }

  private boolean hasPublicObject(Class<? extends IInitializable<?>> requestType) {
    return publicComponents.containsKey(requestType);
  }

  private boolean hasPrivateObject(Class<? extends IInitializable<?>> implementationType) {
    return privateComponents.containsKey(implementationType);
  }

  public void removePublicComponent(Class<? extends IInitializable<?>> requestType) {
    publicComponents.remove(requestType);
  }

  public void removePrivateComponent(Class<? extends IInitializable<?>> implementationType) {
    privateComponents.remove(implementationType);
  }

  private boolean hasDelegate() {
    return delegateObjectFactory != null;
  }

}
