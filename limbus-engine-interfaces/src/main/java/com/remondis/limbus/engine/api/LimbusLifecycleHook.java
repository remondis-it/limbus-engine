package com.remondis.limbus.engine.api;

import java.lang.reflect.Method;

import com.remondis.limbus.api.LimbusPlugin;

/**
 * This interface defines methods of a lifecycle hook used to perform special operations on {@link LimbusPlugin}s.
 *
 * @param <T>
 *        The type of the plugin
 * @author schuettec
 *
 */
public interface LimbusLifecycleHook<T extends LimbusPlugin> {

  /**
   * Called by the Limbus engine before a lifecycle of a {@link LimbusPlugin} is started. Implementors can perform
   * special operations on the specific instance of {@link LimbusPlugin}. <b>Note: The Limbus engine will always take
   * care that any lifecycle hook method is performed within a {@link LimbusContextAction}. The operations a lifecycle
   * hook performs should always be as minimal as possible when working in a {@link LimbusContextAction}.</b>
   *
   * @param limbusPlugin
   *        The Limbus plugin in the state after construction but before initialization.
   *
   * @throws Exception
   *         Can be thrown by the implementation so signal that the {@link LimbusPlugin}
   */
  public void preInitialize(T limbusPlugin) throws Exception;

  /**
   * Method that is called before invocations are passed to {@link LimbusPlugin} and outside the
   * {@link LimbusContextAction}.
   * <p>
   * <b>Note: When using this method, make sure that no plugin code is called. Otherwise it
   * plugin code will be executed outside the {@link LimbusContextAction}.</b>
   * </p>
   * <p>
   * <b>Note: Methods of {@link com.remondis.limbus.api.IInitializable} are not covered by this intercepter method.</b>
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
   * <b>Note: Methods of {@link com.remondis.limbus.api.IInitializable} are not covered by this intercepter method.</b>
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
   * <b>Note: Methods of {@link com.remondis.limbus.api.IInitializable} are not covered by this intercepter method.</b>
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

  /**
   * Called by the Limbus engine after a lifecycle of a {@link LimbusPlugin} was finished. Implementors can perform
   * special operations on the specific instance of {@link LimbusPlugin}. <b>Note: The Limbus engine will always take
   * care that any lifecycle hook method is performed within a {@link LimbusContextAction}. The operations a lifecycle
   * hook performs should always be as minimal as possible when working in a {@link LimbusContextAction}.</b>
   *
   * @param limbusPlugin
   *        The Limbus plugin in the state after finishing the lifecycle.
   */
  public void postFinish(T limbusPlugin);

}
