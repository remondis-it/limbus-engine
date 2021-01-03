package com.remondis.limbus.system;

import static com.remondis.limbus.utils.ReflectionUtil.fieldInjectValue;
import static com.remondis.limbus.utils.ReflectionUtil.setterInjectValue;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.events.EventMulticaster;
import com.remondis.limbus.events.EventMulticasterFactory;
import com.remondis.limbus.system.api.LimbusComponent;
import com.remondis.limbus.system.api.LimbusContainer;
import com.remondis.limbus.system.api.LimbusSystemListener;
import com.remondis.limbus.system.api.ObjectFactory;
import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * The Limbus System is a manager for lifecycle objects of the type {@link IInitializable}. The Limbus System manages
 * the (de)initialization phases and the wiring between objects of the system.
 *
 * <h2>Components</h2>
 * <p>
 * The Limbus System acts as a light-weight Context Dependency Injection where the context is the system of registered
 * components. A component can request a dependency injection for all public components of the same {@link LimbusSystem}
 * .
 * The dependencies are specified using the the injection feature via
 * annotation {@link LimbusComponent}.
 * <b>
 * The Limbus System only keeps track of the registered system components. All components that are not part of the
 * system can only interact with it using the component request methods.
 * </b>
 * </p>
 * <p>
 * Sometimes components need a reference to the instance of the containing {@link LimbusSystem}. Use the annotation
 * {@link LimbusContainer} for this purpose.
 * </p>
 *
 * <h2>(De-)Initialization Order</h2>
 * <p>
 * The Limbus System is responsible for object creation, wiring between the objects and the initialization of
 * {@link IInitializable} objects. The components declared in the system description are initialized in the order they
 * occur, but if a component B that depends on component A, will be initialized after component A.
 * <b>The {@link LimbusSystem} always makes sure, that when a component is initializing, all of it's dependencies are
 * fully initialized.</b>
 * </p>
 * <p>
 * The de-initialization will be performed in the reverse initialization order.
 * </p>
 *
 *
 * <h2>Accessing lifecycle objects</h2>
 * The {@link LimbusSystem} manages two kinds of object types:
 * <ul>
 * <li>Private components: Components that do not have a request type are managed as private components. Private
 * components share the same lifecycle as the containing {@link LimbusSystem} but instances are not accessible from the
 * outside.</li>
 * <li>Public components: Instances of components that specify a request type are accessible using the method
 * {@link #getComponent(Class)}.</li>
 * </ul>
 *
 *
 * @author schuettec
 *
 */
public class LimbusSystem extends Initializable<LimbusSystemException> {

  private static final Logger log = LoggerFactory.getLogger(LimbusSystem.class);

  protected static final ObjectFactory DEFAULT_FACTORY = new ReflectiveObjectFactory();

  protected SystemConfiguration configuration;

  protected ObjectFactory objectFactory;

  protected Map<Class<? extends IInitializable<?>>, Component> publicComponents;

  protected List<Component> allComponents;

  protected List<InfoRecord> infoRecords;

  protected AtomicBoolean denyRequests = new AtomicBoolean(false);

  protected List<Component> initializeOrder;

  protected EventMulticaster<LimbusSystemListener> listeners;

  public LimbusSystem() {
    this.listeners = EventMulticasterFactory.create(LimbusSystemListener.class);

    this.configuration = new SystemConfiguration();
    this.objectFactory = configuration.getObjectFactory();
  }

  public void setObjectFactory(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  protected LimbusSystem(SystemConfiguration configuration) {
    this();
    Lang.denyNull("configuration", configuration);
    this.objectFactory = configuration.getObjectFactory();
    // schuettec - 21.02.2017 : Add all items from the system configuration through the public add methods, because the
    // system configuration may be deserialized in an invalid state.
    addAllFromSystemConfiguration(configuration);
  }

  protected void addAllFromSystemConfiguration(SystemConfiguration configuration) {
    List<ComponentConfiguration> components = configuration.getComponents();
    for (ComponentConfiguration c : components) {
      this.configuration.addComponentConfiguration(c);
    }
  }

  protected List<InfoRecord> lazyInfoRecord() {
    if (infoRecords == null) {
      this.infoRecords = new LinkedList<InfoRecord>();
    }
    return infoRecords;
  }

  /**
   * @return Returns the current state of the Limbus System with the recorded info objects in the order they occurred.
   */
  public List<InfoRecord> getInfoRecords() {
    return new LinkedList<InfoRecord>(infoRecords);
  }

  /**
   * Processes an {@link InfoRecord} if the information feature is enabled.
   *
   * @param supplier
   *        The supplier or a new {@link InfoRecord}. Only evaluated if the info feature is currently enabled.
   */
  protected void infoRecord(Supplier<InfoRecord> supplier) {
    List<InfoRecord> infoRecords = lazyInfoRecord();
    if (infoRecords != null) {
      infoRecords.add(supplier.get());
    }
  }

  /**
   * Checks if a component is available by the specified request type.
   *
   * @param requestType
   *        The component's request type.
   * @return Returns <code>true</code> if a component for this request type is available, otherwise <code>false</code>
   *         is returned.
   */
  public <T extends IInitializable<?>> boolean hasComponentConfigurationFor(Class<T> requestType) {
    return configuration.containsRequestType(requestType);
  }

  /**
   * Adds configuration for a private component.
   *
   * @param componentType
   *        The actual component implementation type.
   * @param failOnError
   *        If <code>true</code> this component causes the {@link LimbusSystem} to fail if the component fails to
   *        initialize. If <code>false</code> the {@link LimbusSystem} performes a successful startup, even if this
   *        component fails to start.
   *
   */
  public <I extends IInitializable<?>> void addComponentConfiguration(Class<I> componentType, boolean failOnError) {
    Lang.denyNull("componentType", componentType);
    ComponentConfiguration component = createComponentConfiguration(componentType, failOnError);
    configuration.addComponentConfiguration(component);
  }

  public <I extends IInitializable<?>> void removePrivateComponentConfiguration(Class<I> componentType) {
    Lang.denyNull("componentType", componentType);
    ComponentConfiguration component = createComponentConfiguration(componentType, false);
    configuration.removeComponentConfiguration(component);
  }

  public <T extends IInitializable<?>> void removePublicComponentConfiguration(Class<T> requestType) {
    Lang.denyNull("requestType", requestType);
    ComponentConfiguration component = createComponentConfiguration(requestType);
    configuration.removeComponentConfiguration(component);
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
   */
  public <T extends IInitializable<?>, I extends T> void addComponentConfiguration(Class<T> requestType,
      Class<I> componentType) {
    Lang.denyNull("componentType", componentType);
    // schuettec - 06.03.2017 : Public components are always required.
    ComponentConfiguration component = createComponentConfiguration(requestType, componentType);
    configuration.addComponentConfiguration(component);
  }

  /**
   * Removes a component configuration from this Limbus System.
   *
   * @param requestType
   *        The component's request type to remove.
   */
  public <T extends IInitializable<?>> void removeComponentConfiguration(Class<T> requestType) {
    configuration.removeByRequestType(requestType);
  }

  /**
   * Provides access to a public component by its request type.
   *
   * @param requestType
   *        The request type of the component.
   * @return Returns the system component.
   */
  public <T extends IInitializable<?>> T getComponent(Class<T> requestType) {
    checkState();
    denyOnDemand();
    // schuettec - 20.02.2017 : The Limbus System is itself a public component
    return ReflectionUtil.getAsExpectedType(getInstance(requestType), requestType);
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
    return _hasComponent(requestType);
  }

  protected boolean _hasComponent(Class<?> requestType) {
    return publicComponents.containsKey(requestType);
  }

  protected Object getInstance(Class<?> requestType) {
    return _getComponent(requestType).getPublicReference();
  }

  protected Component _getComponent(Class<?> requestType) {
    if (publicComponents.containsKey(requestType)) {
      Component component = publicComponents.get(requestType);
      return component;
    } else {
      throw new NoSuchComponentException(requestType);
    }
  }

  protected void denyOnDemand() {
    if (denyRequests.get()) {
      throw new IllegalStateException(
          "The Limbus system is currently starting/stopping and no components can be requested.");
    }
  }

  @Override
  protected void performInitialize() throws LimbusSystemException {
    denyRequests.set(true);
    try {
      this.publicComponents = new ConcurrentHashMap<>();
      this.allComponents = new LinkedList<>();
      this.initializeOrder = new LinkedList<Component>();

      List<ComponentConfiguration> components = configuration.getComponents();
      createAllComponents(components);
      forAllComponents(allComponents, (component) -> {
        /*
         * A component can already be initialized, because another dependency
         * path triggered initialization before.
         */
        if (!initializeOrder.contains(component)) {
          injectDependencies(component);
        }
      });
      forAllComponents(allComponents, (component) -> {
        /*
         * A component can already be initialized, because another dependency
         * path triggered initialization before.
         */
        if (!initializeOrder.contains(component)) {
          initializeComponentOnDemand(component);
        }
      });
      denyRequests.set(false);
      logInfoRecordsOnDemand();
      firePostInitializeEvent();
    } catch (Exception e) {
      logInfoRecordsOnDemand();
      throw e;
    }
  }

  protected void firePostInitializeEvent() {
    listeners.multicastSilently()
        .postInitialize();
  }

  protected void firePreDestroyEvent() {
    listeners.multicastSilently()
        .preDestroy();
    listeners.clear();
    listeners = null;
  }

  /**
   * Logs the current state of the {@link LimbusSystem} with all of its components and initialization states.
   */
  public void logLimbusSystemInformation() {
    logInfoRecords();
  }

  protected void logInfoRecordsOnDemand() {
    logInfoRecords();
  }

  protected void logInfoRecords() {
    StringBuilder b = new StringBuilder("Dumping the component info records collected:\n");
    b.append(InfoRecord.toRecordHeader())
        .append("\n");
    List<InfoRecord> lazyInfoRecord = lazyInfoRecord();
    for (InfoRecord info : lazyInfoRecord) {
      b.append(info.toString())
          .append("\n");
    }
    log.debug(b.toString());
  }

  protected void initializeComponent(Component component) throws LimbusComponentException {
    ComponentConfiguration conf = component.getConfiguration();
    IInitializable<?> instance = component.getInstance();
    try {
      log.debug("Initializing component instance {}.", instance.getClass()
          .getName());
      instance.initialize();
      initializeOrder.add(component);
      infoRecord(initializedRecord(component));
    } catch (Exception e) {
      infoRecord(exceptionRecord(component));
      if (conf.isFailOnError()) {
        throw new LimbusComponentException(requiredComponentFailMessage(conf), e);
      } else {
        log.warn(optionalComponentFailMessage(conf), e);
      }
      removeComponent(component);
    }
  }

  protected void removeComponent(Component component) {
    finishComponentOnDemand(component);
  }

  protected void finishComponentOnDemand(Component component) {
    if (isInitializedComponent(component)) {
      try {
        finishComponent(component);
      } catch (Exception e) {
        handleExceptionOnFinish(e);
      }
    }
  }

  protected void finishComponent(Component component) throws Exception {
    IInitializable<?> instance = component.getInstance();

    try {
      instance.finish();
      infoRecord(finishedRecord(component));
    } catch (Exception e) {
      infoRecord(errorOnFinish(component));
    }
  }

  protected Supplier<InfoRecord> errorOnFinish(Component component) {
    return () -> {
      return new InfoRecord(component, ComponentStatus.ERROR);
    };
  }

  protected Supplier<InfoRecord> finishedRecord(Component component) {
    return () -> {
      return new InfoRecord(component, ComponentStatus.FINISHED);
    };
  }

  protected void createAllComponents(List<ComponentConfiguration> components) throws LimbusSystemException {
    for (ComponentConfiguration conf : components) {
      try {
        createComponent(conf);
        if (log.isDebugEnabled()) {
          log.debug("Component created {}.", conf);
        }
      } catch (LimbusComponentException e) {
        handleComponentException(conf, e);
      }
    }
  }

  protected void handleComponentException(ComponentConfiguration conf, Exception cause) throws LimbusSystemException {
    if (conf.isFailOnError()) {
      throw new LimbusSystemException(requiredComponentFailMessage(conf), cause);
    } else {
      log.warn(optionalComponentFailMessage(conf), cause);
    }
  }

  protected String optionalComponentFailMessage(ComponentConfiguration conf) {
    return String.format("Cannot initialize system component %s - skipping this component because it's optional.",
        conf.getComponentType());
  }

  protected String requiredComponentFailMessage(ComponentConfiguration conf) {
    return String.format("Cannot initialize required system component %s.", conf.getComponentType());
  }

  protected void forAllComponents(Collection<Component> allComponents, ComponentConsumer consumer)
      throws LimbusSystemException {
    for (Component component : allComponents) {
      ComponentConfiguration configuration = component.getConfiguration();
      try {
        consumer.consume(component);
      } catch (LimbusComponentException e) {
        handleComponentException(configuration, e);
      }
    }
  }

  protected void injectDependencies(Component component) throws LimbusComponentException {
    Stack<Component> dependencyPath = new Stack<>();
    _injectDependenciesRecursive(component, dependencyPath);
  }

  protected boolean _injectDependenciesRecursive(Component component, Stack<Component> dependencyPath)
      throws LimbusComponentException {
    // Track path of dependencies
    dependencyPath.add(component);
    IInitializable<?> instance = component.getInstance();
    Class<? extends IInitializable<?>> componentType = component.configuration.getComponentType();
    List<Field> fields = ReflectionUtil.getAllAnnotatedFields(componentType, LimbusContainer.class,
        LimbusComponent.class);
    // schuettec - 02.03.2017 : The current component must be initialized if some of the dependencies were initialized.
    boolean initializedTree = false;
    boolean initializedDependency = false;
    for (Field f : fields) {
      // schuettec - 20.02.2017 : Inject LimbusSystem dependencies
      if (f.isAnnotationPresent(LimbusContainer.class)) {
        injectValue(f, instance, this);
      }

      if (f.isAnnotationPresent(LimbusComponent.class)) {
        // schuettec - 20.02.2017 : Inject component dependencies
        LimbusComponent componentAnnotation = f.getAnnotation(LimbusComponent.class);
        Class<?> requestType = getRequestTypeFromAnnotationOrField(f, componentAnnotation);
        if (_hasComponent(requestType)) {
          Component dependency = _getComponent(requestType);
          // Detect circular dependencies.
          denyCyclicDependencies(dependencyPath, component, dependency);
          // Fork the path for every transitive dependency
          Stack<Component> fork = forkDependencyPath(dependencyPath);
          initializedTree = _injectDependenciesRecursive(dependency, fork);
          initializedDependency = initializeComponentOnDemand(dependency);
          IInitializable<?> dependencyInstance = dependency.getPublicReference();
          injectValue(f, instance, dependencyInstance);
        } else {
          throw new LimbusComponentException(
              String.format("Dependency injection cannot be satisfied: Component %s requires unavailable component %s.",
                  componentType.getName(), requestType.getName()));
        }
      }
    }
    // schuettec - 02.03.2017 : If this action initialized a component, we have to return the result
    boolean hasInitialized = initializedTree || initializedDependency;
    if (hasInitialized) {
      initializeComponentOnDemand(component);
    }
    return hasInitialized;
  }

  protected void injectValue(Field f, Object instance, Object value) throws LimbusComponentException {
    boolean factoryInjectionSuccessfull = this.objectFactory.injectValue(f, instance, value);
    if (!factoryInjectionSuccessfull) {
      boolean setterInjectionSuccessful = setterInjectValue(f, instance, value);
      if (!setterInjectionSuccessful) {
        fieldInjectValue(f, instance, value);
      }
    }
  }

  protected Class<?> getRequestTypeFromAnnotationOrField(Field f, LimbusComponent annotation) {
    if (annotation.value() == Void.class) {
      return f.getType();
    } else {
      return annotation.value();
    }
  }

  protected void denyCyclicDependencies(Stack<Component> dependencyPath, Component requestor, Component dependency) {
    if (dependencyPath.contains(dependency)) {
      int iReq = dependencyPath.indexOf(requestor);
      int iDep = dependencyPath.indexOf(dependency);
      int start = Math.min(iReq, iDep);
      int end = Math.max(iReq, iDep);

      StringBuilder circle = new StringBuilder();
      for (int i = start; i <= end; i++) {
        Component element = dependencyPath.elementAt(i);
        circle.append(element.getConfiguration()
            .getRequestType()
            .getName());
        if (i < end) {
          circle.append(" ~> ");
        }
      }
      throw new LimbusCyclicException(String.format("Cyclic component dependency detected: %s.", circle.toString()));
    }
  }

  protected Stack<Component> forkDependencyPath(Stack<Component> dependencyPath) {
    Stack<Component> newPath = new Stack<>();
    newPath.addAll(dependencyPath);
    return newPath;
  }

  protected boolean initializeComponentOnDemand(final Component dependency) throws LimbusComponentException {
    // schuettec - 09.05.2017 : Do not rely on isInitialized() because this relies to heavy on implementations and
    // mocking is more difficult because we always have to specify the correct answer on invocation of isInitialized().
    if (isInitializedComponent(dependency)) {
      return false;
    } else {
      initializeComponent(dependency);
      return true;
    }
  }

  protected boolean isInitializedComponent(final Component component) {
    return initializeOrder.contains(component);
  }

  protected Supplier<InfoRecord> exceptionRecord(final Component dependency) {
    return () -> {
      ComponentStatus status = ComponentStatus.UNAVAILABLE;
      if (dependency.getConfiguration()
          .isFailOnError()) {
        status = ComponentStatus.ERROR;
      }
      return new InfoRecord(dependency, status);
    };
  }

  protected Supplier<InfoRecord> initializedRecord(final Component dependency) {
    return () -> {
      return new InfoRecord(dependency, ComponentStatus.INITIALIZED);
    };
  }

  protected void createComponent(ComponentConfiguration conf) throws LimbusComponentException {
    Class<? extends IInitializable<?>> componentType = conf.getComponentType();
    try {
      IInitializable<?> instance = null;
      IInitializable<?> publicReference = null;
      if (conf.isPublicComponent()) {
        // Public components need a request type.
        Class<? extends IInitializable<?>> requestType = conf.getRequestType();
        // Add to public components.
        instance = objectFactory.createObject(requestType, componentType);
        publicReference = objectFactory.createPublicReference(requestType, componentType, instance);

        publicComponents.put(requestType, new Component(conf, instance, publicReference));
      } else {
        instance = objectFactory.createObject(componentType);
        publicReference = instance;
      }

      addListenerOnDemand(instance);
      allComponents.add(new Component(conf, instance, publicReference));
    } catch (Exception e) {
      throw new LimbusComponentException(
          String.format("Could not create Limbus component %s.", componentType.getName()), e);
    }
  }

  /**
   * Adds the specified component instance as Limbus System event subscriber if it implements the
   * {@link LimbusSystemListener} interface.
   *
   * @param instance
   *        The component instance to check and subscribe on demand.
   */
  protected void addListenerOnDemand(IInitializable<?> instance) {
    // Add Limbus system event subscriber
    if (instance instanceof LimbusSystemListener) {
      LimbusSystemListener limbusSystemListener = (LimbusSystemListener) instance;
      listeners.addSubscriber(limbusSystemListener);
    }
  }

  @Override
  protected void performFinish() {
    firePreDestroyEvent();

    denyRequests.set(true);
    try {
      List<Component> finishOrder = new LinkedList<>(this.initializeOrder);
      Collections.reverse(finishOrder);

      forAllComponents(finishOrder, (component) -> {
        try {
          log.debug("Finishing component {} ...", component.getInstance()
              .getClass()
              .getName());
          finishComponent(component);
        } catch (Exception e) {
          log.error(String.format("Error while finishing component %s.", component.getInstance()), e);
        }
      });
    } catch (LimbusSystemException e) {
      handleExceptionOnFinish(e);
    } catch (RuntimeException e) {
      handleExceptionOnFinish(e.getCause());
    } finally {
      this.publicComponents = null;
      this.allComponents = null;
      denyRequests.set(false);
      logInfoRecordsOnDemand();
    }

  }

  private <I extends IInitializable<?>> ComponentConfiguration createComponentConfiguration(Class<I> componentType,
      boolean failOnError) {
    return new ComponentConfigurationImpl(componentType, failOnError);
  }

  private <T extends IInitializable<?>> ComponentConfigurationImpl createComponentConfiguration(Class<T> requestType) {
    return new ComponentConfigurationImpl(requestType, null);
  }

  private <T extends IInitializable<?>, I extends T> ComponentConfiguration createComponentConfiguration(
      Class<T> requestType, Class<I> componentType) {
    return new ComponentConfigurationImpl(requestType, componentType, true);
  }

  protected void handleExceptionOnFinish(Throwable e) {
    log.warn(
        "Error while finishing a component. This operation was expected to be silent - this is an implementation fault.",
        e);
  }

  /**
   * Use this method to build a {@link LimbusSystem} from a application class that configures a Limbus application using
   * the
   * {@link LimbusApplication} annotation.
   * 
   * @param applicationClass
   * @return Returns a new {@link LimbusSystem} ready to be initialized.
   */
  public static LimbusSystem fromApplication(Class<?> applicationClass) throws LimbusSystemException {
    SystemConfiguration configuration = ApplicationBuilder.buildConfigurationFromApplicationClass(applicationClass);
    return new LimbusSystem(configuration);
  }

  protected ObjectFactory getObjectFactory() {
    return objectFactory;
  }

}
