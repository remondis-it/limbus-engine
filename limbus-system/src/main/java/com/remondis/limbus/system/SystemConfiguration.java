package com.remondis.limbus.system;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.remondis.limbus.IInitializable;
import com.remondis.limbus.Initializable;
import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.SerializeException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class encapsulates the system configuration that references components to be build and specifies their
 * (de)initialization order.
 *
 * @author schuettec
 *
 */
@XStreamAlias(value = "SystemConfiguration", impl = SystemConfiguration.class)
public final class SystemConfiguration implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Holds the object factory that is to be used.
   */
  protected ObjectFactory objectFactory;

  transient Map<Class<?>, Set<Class>> publicComponents = new Hashtable<>();
  transient Set<Class<?>> privateComponents = new HashSet<>();

  @XStreamImplicit
  public List<ComponentConfiguration> components;

  public static void main(String[] args) throws SerializeException {
    SystemConfiguration conf = new SystemConfiguration();
    conf.setObjectFactory(new ReflectiveObjectFactory());
    conf.addComponentConfiguration(new ComponentConfiguration(Initializable.class, Initializable.class));
    LimbusSystem.DEFAULT_XSTREAM.writeObject(conf, System.out);
  }

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
    // schuettec - 27.03.2017 : Due to the fact that the Limbus System mocking API must be able to replace
    // ComponentConfigurations, we have to update existing configurations!
    if (components.contains(component)) {
      components.remove(component);
    }
    components.add(component);
  }

  public boolean containsComponentConfiguration(ComponentConfiguration component) {
    Lang.denyNull("component", component);
    return components.contains(component);
  }

  /**
   * Adds a new component configuration to the system configuration.
   *
   * @param component
   *        The component configuration to add.
   */
  public void removeComponentConfiguration(ComponentConfiguration component) {
    Lang.denyNull("component", component);
    if (components.contains(component)) {
      components.remove(component);
    }
  }

  /**
   * Removes a component configuration by request type.
   */
  <T extends IInitializable<?>> void removeByRequestType(Class<T> requestType) {
    Lang.denyNull("requestType", requestType);
    ComponentConfiguration conf = new ComponentConfiguration(requestType, null);
    components.remove(conf);
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

  /**
   * Checks if the specified request type is already added to the {@link SystemConfiguration}.
   *
   * @param requestType
   *        The request type to check for.
   * @return Returns <code>true</code> if the specified request type is already added to the {@link SystemConfiguration}
   *         , otherwise <code>false</code> is returned.
   */
  public <T extends IInitializable<?>> boolean containsRequestType(Class<T> requestType) {
    Lang.denyNull("requestType", requestType);
    return containsComponentConfiguration(new ComponentConfiguration(requestType, null));
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
  public Set<Class> getKnownRequestTypes() {
    return components.stream()
        .filter(ComponentConfiguration::isPublicComponent)
        .map(ComponentConfiguration::getRequestType)
        .collect(Collectors.toSet());
  }

  public boolean hasPrivateComponent(Class<? extends IInitializable<?>> componentType) {
    Lang.denyNull("componentType", componentType);
    return containsComponentConfiguration(new ComponentConfiguration(null, componentType));
  }

  public void removePrivateComponent(Class<? extends IInitializable<?>> componentType) {
    Lang.denyNull("componentType", componentType);
    removeComponentConfiguration(new ComponentConfiguration(null, componentType));
  }

}
