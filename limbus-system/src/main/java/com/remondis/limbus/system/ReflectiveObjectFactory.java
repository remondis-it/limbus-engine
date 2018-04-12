package com.remondis.limbus.system;

import com.remondis.limbus.IInitializable;
import com.remondis.limbus.utils.ObjectCreateException;
import com.remondis.limbus.utils.ReflectionUtil;

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
