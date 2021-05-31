package com.remondis.limbus.engine.api;

import java.lang.reflect.Method;

/**
 * Interface to define a plugin invocation interceptor. Use this in conjunction with {@link LimbusLifecycleHook}.
 */
public interface LimbusPluginInterceptor {
  /**
   * Method that is called before invocations are passed to {@link LimbusPlugin} and outside the
   * {@link LimbusContextAction}.
   * <p>
   * <b>Note: When using this method, make sure that no plugin code is called. Otherwise it
   * plugin code will be executed outside the {@link LimbusContextAction}.</b>
   * </p>
   * <p>
   * <b>Note: Methods of {@link com.remondis.limbus.api.IInitializable} are not covered by this interceptor method.</b>
   * </p>
   * 
   * @param plugin The plugin instance.
   * @param proxy The proxy instance.
   * @param method The method to call.
   * @param args The arguments.
   */
  public default void beforeContextInvocation(Object plugin, Object proxy, Method method, Object[] args) {

  }

  /**
   * Method that is called before invocations are passed to {@link LimbusPlugin}. This method is called within the
   * {@link LimbusContextAction}. This way it is safe to call plugin code.
   * </p>
   * <p>
   * <b>Note: Methods of {@link com.remondis.limbus.api.IInitializable} are not covered by this interceptor method.</b>
   * </p>
   * 
   * @param plugin The plugin instance.
   * @param proxy The proxy instance.
   * @param method The method to call.
   * @param args The arguments.
   */
  public default void withinContextInvocation(Object plugin, Object proxy, Method method, Object[] args) {

  }

  /**
   * Method that is called after invocations are passed to {@link LimbusPlugin} and if the plugin code threw an
   * exception/throwable. This method is called within the {@link LimbusContextAction}. This way it is safe to call
   * plugin code.
   * </p>
   * <p>
   * <b>Note: Methods of {@link com.remondis.limbus.api.IInitializable} are not covered by this interceptor method.</b>
   * </p>
   * 
   * @param plugin The plugin instance.
   * @param proxy The proxy instance.
   * @param method The method to call.
   * @param args The arguments.
   * @param t The throwable.
   */
  public default void withinContextError(Object plugin, Object proxy, Method method, Object[] args, Throwable t) {

  }
}
