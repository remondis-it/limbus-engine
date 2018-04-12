package com.remondis.limbus.monitoring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * This {@link InvocationHandler} is a no-Op invocation handler that always
 * returns null. This is used to create no-op {@link Publisher} instances.
 * Due to the fact, that {@link Publisher}s must have only methods with no
 * return types, this no-op implementation is suitable.
 *
 * @author schuettec
 *
 */
class NoopHandler implements InvocationHandler {

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return null;
  }

}
