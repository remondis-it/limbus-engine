package com.remondis.limbus.engine.api;

import java.lang.reflect.Method;

import com.remondis.limbus.api.Classpath;

/**
 * Interface to define a plugin invocation interceptor. Use this in conjunction with {@link LimbusLifecycleHook}.
 */
public interface LimbusPluginInterceptor {

  /**
   * Method that is called before invocations are passed to {@link LimbusPlugin}. This method is called within the
   * {@link LimbusContextAction}. This way it is safe to call plugin code.
   * </p>
   * <p>
   * <b>Note: Methods of {@link com.remondis.limbus.api.IInitializable} are not covered by this interceptor method.</b>
   * </p>
   * 
   * @param classpath The classpath of the current context.
   * @param plugin The plugin instance.
   * @param proxy The proxy instance.
   * @param method The method to call.
   * @param args The arguments.
   * @param interception The interception point to proceed.
   * @return Returns the return value of the plugin invocation.
   * @throws Throwable Thrown on any error.
   */
  public default Object withinContextInvocation(Classpath classpath, Object plugin, Object proxy, Method method,
      Object[] args, Interception interception) throws Throwable {
    return interception.proceed();
  }

}
