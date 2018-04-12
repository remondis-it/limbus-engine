package com.remondis.limbus.system;

import com.remondis.limbus.IInitializable;

/**
 * This class holds information of an instatiated component.
 *
 * @author schuettec
 *
 */
public final class Component {
  protected ComponentConfiguration configuration;
  protected IInitializable<?> instance;
  private IInitializable<?> publicReference;

  Component(ComponentConfiguration configuration, IInitializable<?> instance, IInitializable<?> publicReference) {
    super();
    this.configuration = configuration;
    this.instance = instance;
    this.publicReference = publicReference;
  }

  /**
   * @return the configuration
   */
  public ComponentConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * @return Returns the public reference that was specified by the current object factory.
   */
  public IInitializable<?> getPublicReference() {
    return publicReference;
  }

  /**
   * @return the component
   */
  public IInitializable<?> getInstance() {
    return instance;
  }

  @Override
  public int hashCode() {
    return configuration.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Component other = (Component) obj;
    return configuration.equals(other.getConfiguration());
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Component [\nconfiguration=" + configuration + ",\ninstance=" + instance + "\n]";
  }

}
