package com.remondis.limbus.system;

import java.io.Serializable;

import com.remondis.limbus.IInitializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class defines a component configuration which consists of the type of component to be created and a (super-)type
 * that is used to request this component instance.
 *
 * <p>
 * The identifying attribute for this class is the request type.
 * </p>
 *
 * @author schuettec
 */
@XStreamAlias(value = "Component", impl = ComponentConfiguration.class)
public class ComponentConfiguration implements Serializable {

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
  private Class<? extends IInitializable<?>> componentType;

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
  public <T extends IInitializable<?>, I extends T> ComponentConfiguration(Class<I> componentType,
      boolean failOnError) {
    // schuettec - 16.02.2017 : Private components may not have a request type.
    this.requestType = null;
    this.componentType = componentType;
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
  public <T extends IInitializable<?>, I extends T> ComponentConfiguration(Class<T> requestType,
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
   * @param failOnError
   *        If <code>true</code> this component causes the {@link LimbusSystem} to fail if the component fails to
   *        initialize. If <code>false</code> the {@link LimbusSystem} performes a successful startup, even if this
   *        component fails to start.
   */
  public <T extends IInitializable<?>, I extends T> ComponentConfiguration(Class<T> requestType, Class<I> componentType,
      boolean failOnError) {
    this.requestType = requestType;
    this.componentType = componentType;
    this.failOnError = failOnError;
  }

  /**
   * @return the requestType
   */
  public Class<? extends IInitializable<?>> getRequestType() {
    return requestType;
  }

  /**
   * @return the componentType
   */
  public Class<? extends IInitializable<?>> getComponentType() {
    return componentType;
  }

  /**
   * @return Returns the fail on error flag.
   */
  public boolean isFailOnError() {
    return failOnError;
  }

  /**
   * @param failOnError
   *        Sets the fail on error flag.
   */
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  /**
   * @return Returns <code>true</code> if this component was configured to be public accessible. Otherwise
   *         <code>false</code>
   */
  public boolean isPublicComponent() {
    return requestType != null;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((requestType == null) ? 0 : requestType.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ComponentConfiguration other = (ComponentConfiguration) obj;
    if (requestType == null) {
      if (other.requestType == null) {
        return this.getComponentType()
            .equals(other.getComponentType());
      } else {
        return false;
      }
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
