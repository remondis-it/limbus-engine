package org.max5.limbus.system;

import org.max5.limbus.IInitializable;
import org.max5.limbus.utils.ObjectCreateException;
import org.max5.limbus.utils.ReflectionUtil;

/**
 * This {@link ObjectFactory} creates object using reflections.
 *
 * @author schuettec
 *
 */
public class ReflectiveObjectFactory implements ObjectFactory {

  @Override
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> implementationType) throws ObjectCreateException {
    return ReflectionUtil.newInstance(requestType, implementationType);
  }

  @Override
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> implementationType)
      throws ObjectCreateException {
    return ReflectionUtil.newInstance(IInitializable.class, implementationType);
  }

}
