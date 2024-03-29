package com.remondis.limbus.system;

import static java.util.Objects.isNull;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import com.remondis.limbus.api.IInitializable;

/**
 * This class defines a component configuration which consists of the type of component to be created and a (super-)type
 * that is used to request this component instance.
 *
 * @author schuettec
 */
public class ComponentConfigurationImpl implements Serializable, ComponentConfiguration {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * This flag specifies if the {@link LimbusSystem} should fail to start if this component cannot be initialized.
   */
  private boolean failOnError = false;

  /**
   * (Optional) The request type for this component. If the request type is set, the component can be requested using
   * the {@link LimbusSystem}. If <code>null</code> the component is not accessible through {@link LimbusSystem}.
   */
  private Class<? extends IInitializable<?>> requestType;

  /**
   * The implementation type for this component.
   */
  private WeakReference<Class<? extends IInitializable<?>>> componentType;

  /**
   * Creates a new private component configuration with the specified required flag.
   * <p>
   * A component may cause the {@link LimbusSystem} to fail it's startup if the component fails to initialize.
   * Optional components just remain unavailable on error, but the {@link LimbusSystem} starts successfully.
   * </p>
   *
   * @param componentType
   *        The actual component implementation type.
   * @param failOnError
   *        If <code>true</code> this component causes the {@link LimbusSystem} to fail if the component fails to
   *        initialize. If <code>false</code> the {@link LimbusSystem} performes a successful startup, even if this
   *        component fails to start.
   *
   */
  protected <T extends IInitializable<?>, I extends T> ComponentConfigurationImpl(Class<I> componentType,
      boolean failOnError) {
    // schuettec - 16.02.2017 : Private components may not have a request type.
    this.requestType = null;
    this.componentType = new WeakReference<Class<? extends IInitializable<?>>>(componentType);
    this.failOnError = failOnError;
  }

  /**
   * Creates a new public component configuration that is configured to be required.
   * <p>
   * This component causes the {@link LimbusSystem} to fail it's startup if the component fails to initialize.
   * </p>
   *
   * @param requestType
   *        The request type of the component.
   * @param componentType
   *        The actual component implementation type.
   *
   */
  protected <T extends IInitializable<?>, I extends T> ComponentConfigurationImpl(Class<T> requestType,
      Class<I> componentType) {
    this(requestType, componentType, true);
  }

  /**
   * Creates a new public component configuration with the specified required flag.
   * <p>
   * A component may cause the {@link LimbusSystem} to fail it's startup if the component fails to initialize.
   * Optional components just remain unavailable on error, but the {@link LimbusSystem} starts successfully.
   * </p>
   *
   * @param requestType
   *        The request type of the component.
   * @param componentType
   *        The actual component implementation type.
   * @param failOnErrorComponentConfiguration
   *        If <code>true</code> this component causes the {@link LimbusSystem} to fail if the component fails to
   *        initialize. If <code>false</code> the {@link LimbusSystem} performes a successful startup, even if this
   *        component fails to start.
   */
  protected <T extends IInitializable<?>, I extends T> ComponentConfigurationImpl(Class<T> requestType,
      Class<I> componentType, boolean failOnError) {
    this.requestType = requestType;
    this.componentType = new WeakReference<Class<? extends IInitializable<?>>>(componentType);
    this.failOnError = failOnError;
  }

  /**
   * @return the requestType
   */
  @Override
  public Class<? extends IInitializable<?>> getRequestType() {
    return requestType;
  }

  /**
   * @return the componentType
   */
  @Override
  public Class<? extends IInitializable<?>> getComponentType() {
    Class<? extends IInitializable<?>> reference = componentType.get();
    if (isNull(reference)) {
      throw new NoSuchComponentException(
          "The specified component type is not available. Maybe the target was undeployed?");
    } else {
      return reference;
    }
  }

  /**
   * @return Returns the fail on error flag.
   */
  @Override
  public boolean isFailOnError() {
    return failOnError;
  }

  /**
   * @param failOnError
   *        Sets the fail on error flag.
   */
  @Override
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  /**
   * @return Returns <code>true</code> if this component was configured to be public accessible. Otherwise
   *         <code>false</code>
   */
  @Override
  public boolean isPublicComponent() {
    return requestType != null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
    result = prime * result + ((requestType == null) ? 0 : requestType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ComponentConfigurationImpl other = (ComponentConfigurationImpl) obj;
    if (componentType == null) {
      if (other.componentType != null)
        return false;
    } else if (!componentType.equals(other.componentType))
      return false;
    if (requestType == null) {
      if (other.requestType != null)
        return false;
    } else if (!requestType.equals(other.requestType))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ComponentConfiguration [failOnError=" + failOnError + ", requestType=" + requestType + ", componentType="
        + componentType + "]";
  }

}
