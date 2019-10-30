package com.remondis.limbus.monitoring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.remondis.limbus.monitoring.publisher.Runtime;
import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * This is an implementation of an {@link InvocationHandler} that determines the runtime of method calls and publishes
 * the measured runtimes to the Limbus Monitoring Facade. The invocation handler maintains the reference to it's
 * original component instance for delegation of real calls.
 *
 * @author schuettec
 *
 */
public class MonitoringProxy implements InvocationHandler {

  private static final Monitoring monitor = MonitoringFactory.getMonitoring(MonitoringProxy.class);

  private Object delegate;

  protected MonitoringProxy(Object delegate) {
    Lang.denyNull("delegate", delegate);
    this.delegate = delegate;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    long start = System.currentTimeMillis();
    Object retValue = ReflectionUtil.invokeMethodProxySafe(method, delegate, args);
    long stop = System.currentTimeMillis();
    monitor.publish(Runtime.class)
        .publishRuntime(stop, delegate.getClass()
            .getName(), method.getName(), null, null, stop - start);
    return retValue;
  }

}
