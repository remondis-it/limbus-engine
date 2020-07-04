package com.remondis.limbus.system.api;

import java.lang.reflect.Field;

import com.remondis.limbus.api.IInitializable;

/**
 * This interface defines an object factory that is used by the
 * {@link LimbusSystem} to create objects.
 *
 * 
 *
 */
public interface ObjectFactory {

  /**
   * Creates a new instance of a public component.
   *
   * @param requestType The request type the created instance is returned
   *        as, must be a super type of the implementation
   *        type.
   * @param implementationType The implementation type to instantiate.
   * @return Returns the created object casted to the specified super type.
   * @throws ObjectCreateException Thrown if the object creation failed.
   * @throws SecurityException Thrown if the runtime does not grant the
   *         permissions to reflectively create an object.
   */
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> implementationType) throws Exception;

  /**
   * Creates a new instance of a private component.
   *
   * @param implementationType the type to instantiate
   * @return Returns the created object casted to the specified super type.
   * @throws ObjectCreateException Thrown if the object creation failed.
   * @throws SecurityException Thrown if the runtime does not grant the
   *         permissions to reflectively create an object.
   */
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> implementationType) throws Exception;

  /**
   * Called by {@link LimbusSystem} to create a reference to the specified public
   * component. Gives object factories the chance to create a proxy object.
   *
   * @param requestType The request type of the public component
   * @param componentType The component implementation type
   * @param instance The original instance of the component
   * @return Returns an object that is used to access the component instance.
   *         Implementations may return proxy objects here.
   */
  public default IInitializable<?> createPublicReference(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> componentType, IInitializable<?> instance) {
    return instance;
  }

  /**
   * Injects a value into the specified instance. The injection strategy is
   * defined by this method.
   * 
   * @param f The field to inject.
   * @param instance The instance to injecto into.
   * @param value The value to inject.
   * @return Returns <code>true</code> if the injection was successfull. If
   *         <code>false</code> is returned, the framework tries some fallback
   *         methods to inject the value.
   */
  public default boolean injectValue(Field f, Object instance, Object value) {
    return false;
  }

}
