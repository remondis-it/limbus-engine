package com.remondis.limbus.engine.api;

import com.remondis.limbus.api.IInitializable;

/**
 * This interface describes a consumer for default components. The consumer will be passed to {@link DefaultComponents}
 * so engine implementations can provide a minimal system configuration description for use with Limbus Staging.
 *
 * @author schuettec
 *
 */
public interface DefaultComponentsConsumer {

  /**
   * Adds configuration for a private component.
   *
   * @param componentType
   *        The actual component implementation type.
   * @param failOnError
   *        If <code>true</code> this component causes the Limbus System to fail if the component fails to
   *        initialize. If <code>false</code> the Limbus System performes a successful startup, even if this
   *        component fails to start.
   * @return Returns this object for method chainging.
   *
   */
  public <I extends IInitializable<?>> Object addComponentConfiguration(Class<I> componentType, boolean failOnError);

  /**
   * Adds configuration for a required public component to this default component consumer.
   *
   * <p>
   * This component causes the Limbus System to fail it's startup if the component fails to initialize.
   * </p>
   *
   * @param requestType
   *        The component type, used for component requests.
   * @param componentType
   *        The actual component implementation type.
   * @return Returns this object for method chaining.
   */
  public <T extends IInitializable<?>, I extends T> Object addComponentConfiguration(Class<T> requestType,
      Class<I> componentType);
}
