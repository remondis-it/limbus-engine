package com.remondis.limbus.system;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.system.api.ObjectFactory;
import com.remondis.limbus.utils.Lang;

/**
 * This class encapsulates the system configuration that references components to be build and specifies their
 * (de)initialization order.
 *
 * @author schuettec
 *
 */
public final class SystemConfiguration implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Holds the object factory that is to be used.
   */
  protected ObjectFactory objectFactory;

  protected List<ComponentConfiguration> components;

  /**
   * Creates a new {@link SystemConfiguration} containig all component configurations from the specified configuration.
   *
   * @param configuration
   *        The configuration to copy.
   */
  public SystemConfiguration(SystemConfiguration configuration) {
    Lang.denyNull("configuration", configuration);
    this.components = new LinkedList<>(configuration.components);
    this.objectFactory = configuration.objectFactory;
  }

  public SystemConfiguration() {
    readResolve();
  }

  protected Object readResolve() {
    if (components == null) {
      components = new LinkedList<ComponentConfiguration>();
    }
    if (objectFactory == null) {
      objectFactory = LimbusSystem.DEFAULT_FACTORY;
    }
    return this;
  }

  /**
   * Adds a new component configuration to the system configuration.
   *
   * @param component
   *        The component configuration to add.
   */
  public void addComponentConfiguration(ComponentConfiguration component) {
    Lang.denyNull("component", component);
    components.add(component);
  }

  /**
   * Adds a new component configuration to the system configuration.
   *
   * @param component
   *        The component configuration to add.
   */
  public void addAndOverrideComponentConfiguration(ComponentConfiguration component) {
    Lang.denyNull("component", component);
    this.removePublicComponent(component.getRequestType());
    components.add(component);
  }

  /**
   * Removes a component configuration by request type.
   */
  <T extends IInitializable<?>> void removePublicComponent(Class<T> requestType) {
    Lang.denyNull("requestType", requestType);
    components.removeIf(compConf -> compConf.isPublicComponent() && compConf.getRequestType()
        .equals(requestType));
  }

  /**
   * @return the objectFactory
   */
  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  /**
   * @param objectFactory
   *        the objectFactory to set
   */
  public void setObjectFactory(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  /**
   * @return the components Returns a new list containig the configured components.
   */
  public List<ComponentConfiguration> getComponents() {
    return new LinkedList<>(components);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder("System Configuration\n");
    for (ComponentConfiguration c : components) {
      b.append(c.toString())
          .append("\n");
    }
    return b.toString();
  }

  /**
   * @return Returns a {@link Set} of all known request types.
   */
  public Set<Class<?>> getKnownRequestTypes() {
    return components.stream()
        .filter(ComponentConfiguration::isPublicComponent)
        .map(ComponentConfiguration::getRequestType)
        .collect(Collectors.toSet());
  }

  public boolean hasPrivateComponent(Class<? extends IInitializable<?>> componentType) {
    Lang.denyNull("componentType", componentType);
    return getAllPrivateComponentTypes().contains(componentType);
  }

  private List<Class<?>> getAllPrivateComponentTypes() {
    return components.stream()
        .filter(Predicate.not(ComponentConfiguration::isPublicComponent))
        .map(ComponentConfiguration::getComponentType)
        .collect(toList());
  }

  public void removePrivateComponent(Class<? extends IInitializable<?>> componentType) {
    Lang.denyNull("componentType", componentType);
    components.removeIf(compConf -> compConf.getComponentType()
        .equals(componentType));
  }

  public boolean containsRequestType(Class<?> requestType) {
    return getKnownRequestTypes().contains(requestType);
  }

}
