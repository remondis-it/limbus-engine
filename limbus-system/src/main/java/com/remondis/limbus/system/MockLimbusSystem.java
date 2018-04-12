package com.remondis.limbus.system;

import java.io.InputStream;
import java.util.List;

import com.remondis.limbus.IInitializable;
import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.SerializeException;

/**
 * This is a facade for {@link LimbusSystem} that should only be used in test environments to specify concrete instances
 * for the components managed by the system.
 *
 * @author schuettec
 *
 */
public class MockLimbusSystem implements IInitializable<Exception> {

  private LimbusSystem system;

  private MockObjectFactory objectFactory;

  public MockLimbusSystem() {
    this.system = new LimbusSystem();
    setupFields();
  }

  public MockLimbusSystem(InputStream limbusSystemXML) throws SerializeException {
    Lang.denyNull("limbusSystemXML", limbusSystemXML);
    this.system = LimbusSystem.deserializeConfiguration(limbusSystemXML);
    setupFields();
  }

  private void setupFields() {
    ObjectFactory delegate = system.getObjectFactory();
    this.objectFactory = new MockObjectFactory(delegate);
    system.setObjectFactory(this.objectFactory);
  }

  /**
   * @return Returns the list of info records.
   * @see com.remondis.limbus.system.LimbusSystem#getInfoRecords()
   */
  public List<InfoRecord> getInfoRecords() {
    return system.getInfoRecords();
  }

  /**
   * Returns a component instance from this {@link MockLimbusSystem}.
   *
   * @param requestType
   *        The request type of the component
   * @return Returns the component instance
   * @see com.remondis.limbus.system.LimbusSystem#getComponent(java.lang.Class)
   */
  public <T extends IInitializable<?>> T getComponent(Class<T> requestType) {
    return system.getComponent(requestType);
  }

  /**
   * Checks if a Limbus component is available through this Limbus System.
   *
   * @param requestType
   *        The request type of the component.
   * @return Returns <code>true</code> if the component is available, <code>false</code> otherwise.
   *
   */
  public <T extends IInitializable<?>> boolean hasComponent(Class<T> requestType) {
    return system.hasComponent(requestType);
  }

  /**
   * @see com.remondis.limbus.system.LimbusSystem#logLimbusSystemInformation()
   */
  public void logLimbusSystemInformation() {
    system.logLimbusSystemInformation();
  }

  /**
   * @return Returns the {@link LimbusSystem} prepared with configuration or mock instances.
   */
  public LimbusSystem getLimbusSystem() {
    return system;
  }

  /**
   * Adds a private component configuration to this representation of {@link LimbusSystem}.
   *
   * @param componentType
   *        The component type of the private component.
   * @param failOnError
   *        If <code>true</code> this component causes the {@link LimbusSystem} to fail if the component fails to
   *        initialize. If <code>false</code> the {@link LimbusSystem} performes a successful startup, even if this
   *        component fails to start.
   * @return Returns this object for method chaining.
   */
  public <I extends IInitializable<?>> MockLimbusSystem addComponentConfiguration(Class<I> componentType,
      boolean failOnError) {
    system.addComponentConfiguration(componentType, failOnError);
    return this;
  }

  /**
   * Removes a private component configuration to this representation of {@link LimbusSystem}.
   *
   * @param componentType
   *        The component type of the private component.
   * @return Returns this object for method chaining.
   */
  public <I extends IInitializable<?>> MockLimbusSystem removePrivateComponentConfiguration(Class<I> componentType) {
    system.removePrivateComponentConfiguration(componentType);
    return this;
  }

  /**
   * Adds configuration for a required public component to this Limbus System.
   *
   * <p>
   * This component causes the {@link LimbusSystem} to fail it's startup if the component fails to initialize.
   * </p>
   *
   * @param requestType
   *        The component type, used for component requests.
   * @param componentType
   *        The actual component implementation type.
   * @return Returns this object for method chaining.
   * @see com.remondis.limbus.system.LimbusSystem#addComponentConfiguration(java.lang.Class, java.lang.Class)
   */
  public <T extends IInitializable<?>, I extends T> MockLimbusSystem addComponentConfiguration(Class<T> requestType,
      Class<I> componentType) {
    system.addComponentConfiguration(requestType, componentType);
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
  public <T extends IInitializable<?>> MockLimbusSystem removePublicComponentConfiguration(Class<T> requestType) {
    system.removePublicComponentConfiguration(requestType);
    return this;
  }

  /**
   * Adds a private component with a mock instance.
   *
   * @param implementationType
   *        The implementation type of this component.
   * @param instance
   *        The mock instance
   * @param failOnError
   *        If <code>true</code> this component causes the {@link LimbusSystem} to fail if the component fails to
   *        initialize. If <code>false</code> the {@link LimbusSystem} performes a successful startup, even if this
   *        component fails to start.
   * @return Returns this object for method chaining.
   */
  public MockLimbusSystem addPrivateComponentMock(Class<? extends IInitializable<?>> implementationType,
      IInitializable<?> instance, boolean failOnError) {
    objectFactory.putPrivateComponent(implementationType, instance);
    system.addComponentConfiguration(implementationType, failOnError);
    return this;

  }

  /**
   * Removes a private component with a mock instance.
   *
   * @param implementationType
   *        The implementation type of this component.
   * @return Returns this object for method chaining.
   */
  public MockLimbusSystem removePrivateComponentMock(Class<? extends IInitializable<?>> implementationType) {
    objectFactory.removePrivateComponent(implementationType);
    system.removePrivateComponentConfiguration(implementationType);
    return this;

  }

  /**
   * Adds a public required component with the specified mock instance.
   *
   * @param requestType
   *        The request type of the public component.
   * @param instance
   *        The mock instance of this component.
   * @return Returns this object for method chaining.
   */
  @SuppressWarnings("unchecked")
  public <T extends IInitializable<?>, I extends T> MockLimbusSystem addPublicComponentMock(Class<T> requestType,
      I instance) {
    objectFactory.putPublicComponent(requestType, instance);
    system.addComponentConfiguration(requestType, (Class<I>) instance.getClass());
    return this;
  }

  /**
   * Removes a public required component with the specified mock instance.
   *
   * @param requestType
   *        The request type of the public component.
   * @return Returns this object for method chaining.
   */
  public <T extends IInitializable<?>> MockLimbusSystem removePublicComponentMock(Class<T> requestType) {
    objectFactory.removePublicComponent(requestType);
    system.removePublicComponentConfiguration(requestType);
    return this;
  }

  @Override
  public void initialize() throws Exception {
    system.initialize();
  }

  @Override
  public void finish() {
    system.finish();
  }

}
