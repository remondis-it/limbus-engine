package com.remondis.limbus.engine.api;

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
