package com.remondis.limbus.system;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.system.api.ObjectFactory;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * This {@link ObjectFactory} creates object using reflections.
 *
 * 
 *
 */
public class ReflectiveObjectFactory implements ObjectFactory {

  @Override
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> implementationType) throws Exception {
    return ReflectionUtil.newInstance(requestType, implementationType);
  }

  @Override
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> implementationType) throws Exception {
    return ReflectionUtil.newInstance(IInitializable.class, implementationType);
  }

}
