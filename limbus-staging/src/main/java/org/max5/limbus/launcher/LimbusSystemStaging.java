package org.max5.limbus.launcher;

import java.io.InputStream;

import org.max5.limbus.DefaultComponentsConsumer;
import org.max5.limbus.IInitializable;
import org.max5.limbus.system.LimbusSystem;
import org.max5.limbus.system.MockLimbusSystem;
import org.max5.limbus.utils.SerializeException;

public class LimbusSystemStaging implements DefaultComponentsConsumer {

  private LimbusStage stage;
  private MockLimbusSystem mockSystem;

  public LimbusSystemStaging(LimbusStage stage) {
    this.stage = stage;
    this.mockSystem = new MockLimbusSystem();
  }

  public LimbusSystemStaging(LimbusStage stage, InputStream limbusSystemXML) throws SerializeException {
    this.stage = stage;
    this.mockSystem = new MockLimbusSystem(limbusSystemXML);
  }

  /**
   * @return Builds the {@link LimbusStage} that holds all information and configurations to start an embedded Limbus
   *         Environment.
   */
  public LimbusStage buildStage() {
    this.stage.setSystem(mockSystem.getLimbusSystem());
    return this.stage;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.max5.limbus.system.MockLimbusSystem#addComponentConfiguration(java.lang.Class, boolean)
   */
  @Override
  public <I extends IInitializable<?>> LimbusSystemStaging addComponentConfiguration(Class<I> componentType,
      boolean failOnError) {
    mockSystem.addComponentConfiguration(componentType, failOnError);
    return this;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.max5.limbus.system.MockLimbusSystem#addComponentConfiguration(java.lang.Class, java.lang.Class)
   */
  @Override
  public <T extends IInitializable<?>, I extends T> LimbusSystemStaging addComponentConfiguration(Class<T> requestType,
      Class<I> componentType) {
    mockSystem.addComponentConfiguration(requestType, componentType);
    return this;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.max5.limbus.system.MockLimbusSystem#addPrivateComponentMock(java.lang.Class,
   * org.max5.limbus.IInitializable, boolean)
   */
  public LimbusSystemStaging addPrivateComponentMock(Class<? extends IInitializable<?>> implementationType,
      IInitializable<?> instance, boolean failOnError) {
    mockSystem.addPrivateComponentMock(implementationType, instance, failOnError);
    return this;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.max5.limbus.system.MockLimbusSystem#addPublicComponentMock(java.lang.Class,
   * org.max5.limbus.IInitializable)
   */
  public <T extends IInitializable<?>, I extends T> LimbusSystemStaging addPublicComponentMock(Class<T> requestType,
      I instance) {
    mockSystem.addPublicComponentMock(requestType, instance);
    return this;
  }

  /**
   * Removes a private component configuration to this representation of {@link LimbusSystem}.
   *
   * @param componentType
   *        The component type of the private component.
   * @return Returns this object for method chaining.
   */
  public <I extends IInitializable<?>> LimbusSystemStaging removePrivateComponentConfiguration(Class<I> componentType) {
    mockSystem.removePrivateComponentConfiguration(componentType);
    return this;
  }

  /**
   * Removes configuration for a required public component to this Limbus System.
   *
   * <p>
   * This component causes the {@link LimbusSystem} to fail it's startup if the component fails to initialize.
   * </p>
   *
   * @param requestType
   *        The component type, used for component requests.
   * @return Returns this object for method chaining.
   */
  public <T extends IInitializable<?>> LimbusSystemStaging removePublicComponentConfiguration(Class<T> requestType) {
    mockSystem.removePublicComponentConfiguration(requestType);
    return this;
  }

  /**
   * Removes a private component with a mock instance.
   *
   * @param implementationType
   *        The implementation type of this component.
   * @return Returns this object for method chaining.
   */
  public LimbusSystemStaging removePrivateComponentMock(Class<? extends IInitializable<?>> implementationType) {
    mockSystem.removePrivateComponentMock(implementationType);
    return this;
  }

  /**
   * Removes a public required component with the specified mock instance.
   *
   * @param requestType
   *        The request type of the public component.
   * @return Returns this object for method chaining.
   */
  public <T extends IInitializable<?>> LimbusSystemStaging removePublicComponentMock(Class<T> requestType) {
    mockSystem.removePublicComponentMock(requestType);
    return this;
  }

}
