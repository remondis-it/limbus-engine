package com.remondis.limbus.system;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.utils.ObjectCreateException;

/**
 * This interface defines an object factory that is used by the {@link LimbusSystem} to create objects.
 *
 * @author schuettec
 *
 */
public interface ObjectFactory {

  /**
   * Creates a new instance of a public component.
   *
   * @param requestType
   *        The request type the created instance is returned as, must be a super type of the implementation type.
   * @param implementationType
   *        The implementation type to instantiate.
   * @return Returns the created object casted to the specified super type.
   * @throws ObjectCreateException
   *         Thrown if the object creation failed.
   * @throws SecurityException
   *         Thrown if the runtime does not grant the permissions to reflectively create an object.
   */
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> implementationType) throws ObjectCreateException;

  /**
   * Creates a new instance of a private component.
   *
   * @param implementationType
   *        the type to instantiate
   * @return Returns the created object casted to the specified super type.
   * @throws ObjectCreateException
   *         Thrown if the object creation failed.
   * @throws SecurityException
   *         Thrown if the runtime does not grant the permissions to reflectively create an object.
   */
  public IInitializable<?> createObject(Class<? extends IInitializable<?>> implementationType)
      throws ObjectCreateException;

  /**
   * Called by {@link LimbusSystem} to create a reference to the specified public component. Gives object factories the
   * chance to create a proxy object.
   *
   * @param requestType
   *        The request type of the public component
   * @param componentType
   *        The component implementation type
   * @param instance
   *        The original instance of the component
   * @return Returns an object that is used to access the component instance. Implementations may return proxy objects
   *         here.
   */
  public default IInitializable<?> createPublicReference(Class<? extends IInitializable<?>> requestType,
      Class<? extends IInitializable<?>> componentType, IInitializable<?> instance) {
    return instance;
  }

}
