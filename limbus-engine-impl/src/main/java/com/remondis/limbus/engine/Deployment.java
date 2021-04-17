package com.remondis.limbus.engine;

import static com.remondis.limbus.engine.LimbusUtil.denyClassNotFound;
import static com.remondis.limbus.engine.LimbusUtil.isLimbusPlugin;

import java.lang.reflect.Proxy;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.IllegalTypeException;
import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.api.LimbusClasspathException;
import com.remondis.limbus.api.LimbusException;
import com.remondis.limbus.api.LimbusPlugin;
import com.remondis.limbus.engine.api.LimbusContext;
import com.remondis.limbus.engine.api.LimbusContextAction;
import com.remondis.limbus.engine.api.LimbusLifecycleHook;
import com.remondis.limbus.events.EventMulticaster;
import com.remondis.limbus.events.EventMulticasterFactory;
import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * The Deployment provides access to all needed information for a deployed plugin to execute an action.
 *
 * @author buschmann
 *
 */
class Deployment extends Initializable<LimbusClasspathException> {

  private static final Logger log = LoggerFactory.getLogger(Deployment.class);

  // schuettec - 27.01.2017 : This is a list holding the only strong references to Limbus plugins. Strong references
  // should not be made accessible to the outside. This should be the only point in the whole Limbus Engine that is able
  // to hold a strong reference to a plugin.
  private List<LimbusPlugin> strongReferences;

  private ConcurrentHashMap<String, LimbusPlugin> pluginRegistry;

  private EventMulticaster<LimbusPlugin> lifecycleMulticaster;

  private LimbusContextInternal limbusContext;

  private Classpath classpath;

  Deployment(Classpath classpath, PluginClassLoader classloader) {
    Lang.denyNull("Classpath", classpath);
    Lang.denyNull("Classloader", classloader);

    this.classpath = classpath;
    this.pluginRegistry = new ConcurrentHashMap<String, LimbusPlugin>();
    this.limbusContext = new LimbusContextInternal(classloader);
    this.lifecycleMulticaster = EventMulticasterFactory.create(LimbusPlugin.class);
    this.strongReferences = new LinkedList<>();
  }

  /**
   * Returns a Limbus plugin instance from this deployment. If the plugin class is known by this deployment and was not
   * created and initialized before, this method creates the plugin instance, creates the lifecycle hook if specified
   * and initializes the plugin.
   *
   * @param classname
   *        The classname of the plugin
   * @param pluginInterface
   *        The interface the plugin implements.
   * @param lifecycleHook
   *        (Optional) The lifecycle hook that intercepts the plugin's lifecycle methods.
   * @return Returns the plugin instance as the expected type if creation and initialization was successfull.
   * @throws LimbusException
   *         Thrown if the creation or initialization failed.
   */
  protected <T extends LimbusPlugin> T getPlugin(String classname, Class<T> pluginInterface,
      LimbusLifecycleHook<T> lifecycleHook, boolean initialize) throws LimbusException {
    try {
      denyNotALimbusPluginInterface(pluginInterface);
      if (pluginRegistry.containsKey(classname)) {
        return getPluginFromRegistry(classname, pluginInterface);
      } else {
        // schuettec - 26.01.2017 : feature-66 Search for class in Plugin-Classpath and if found, initialize it and put
        // it into cache
        T limbusPlugin = createPlugin(classname, pluginInterface);
        limbusPlugin = initializePlugin(classname, pluginInterface, lifecycleHook, limbusPlugin, initialize);
        return limbusPlugin;
      }
    } catch (LimbusClasspathException e) {
      // schuettec - 27.01.2017 : Wrap the classpath exception into a LimbusException. It is indeed worth to throw a
      // classpath exception here, but this would change the minor version and breaks backwards compatibility.
      String message;
      if (classpath.hasDeployName()) {
        message = String.format("Error while accessing plugin %s for plugin interface %s in deployment %s.", classname,
            pluginInterface.getName(), classpath.getDeployName());
      } else {
        message = String.format("Error while accessing plugin %s for plugin interface %s.", classname,
            pluginInterface.getName());
      }
      throw new LimbusException(message, e);
    }
  }

  /**
   * Provides access to deployed plugins of this {@link LimbusEngine}. This method creates a proxy around the plugin
   * instance implementing the specified supported interface. This is done so that the returned proxy can be cast to
   * this particular types. <b>Note: The plugin must implement the specified supported interfaces!</b>
   * 
   * @param <S> The supported interface.
   * @param <T> The plugin type
   * @param classpath
   *        The classpath the plugin is expected to be available in.
   * @param classname The classname of the plugin.
   * @param pluginInterface The plugin type .
   * @param supportedIntefaces The interface the plugin proxy should support. <strong>Note: The plugin must implement
   *        the
   *        specified interface.</strong>
   * @param toDefineIn The {@link ClassLoader} to define the proxy class in.
   * @param lifecycleHook
   *        (Optional) The lifecycle hook to be executed. <b>The lifecycle hook specified here will only be set on
   *        first plugin request.</b>
   * @param initialize If <code>true</code> the plugin is initialized, if <code>false</code> the caller is responsible
   *        to initialize the plugin using a {@link LimbusContextAction}.
   * @return Returns a proxy to the plugin instance that supports the specified interface.
   * @throws LimbusException
   *         Thrown if the plugin could not be initialized, the plugin was not found, the classpath is not
   *         deployed or the expected type does not match.
   * 
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  protected <S, T extends LimbusPlugin> S createPluginProxy(String classname, Class<T> pluginInterface,
      Class<S>[] supportedIntefaces, ClassLoader toDefineIn, LimbusLifecycleHook<T> lifecycleHook, boolean initialize)
      throws LimbusException {
    try {
      T plugin = getPlugin(classname, pluginInterface, lifecycleHook, initialize);
      LifecycleProxyHandler invocationHandler = (LifecycleProxyHandler) Proxy.getInvocationHandler(plugin);
      // schuettec - 27.01.2017 : Let the proxy will be defined in the classloader of the supported interface.
      Class<?>[] interfaces = new Class<?>[supportedIntefaces.length + 1];
      interfaces[0] = pluginInterface;
      System.arraycopy(supportedIntefaces, 0, interfaces, 1, supportedIntefaces.length);
      return (S) Proxy.newProxyInstance(toDefineIn, interfaces, invocationHandler);
    } catch (Exception | LinkageError e) {
      // schuettec - 27.01.2017 : Wrap the classpath exception into a LimbusException. It is indeed worth to throw a
      // classpath exception here, but this would change the minor version and breaks backwards compatibility.
      String message;
      if (classpath.hasDeployName()) {
        message = String.format("Error while accessing plugin %s for plugin interface %s in deployment %s.", classname,
            pluginInterface.getName(), classpath.getDeployName());
      } else {
        message = String.format("Error while accessing plugin %s for plugin interface %s.", classname,
            pluginInterface.getName());
      }
      throw new LimbusException(message, e);
    }
  }

  /**
   * This method processes a plugin instance that is assumed to not being cached. This method takes care of creating a
   * strong reference to the plugin wich is assumed to be the only strong reference to this plugin instance. Then a
   * proxy object is created to interact with the plugin instance. The proxy object itself only holds a weak reference
   * to the plugin. After this the plugin is initialized and the plugin type is added to the local plugin cache.
   *
   * @param The
   *        classname of the requested plugin. <b>Important: The classname of the plugin is the identifying element
   *        for an instance. This ensures that the plugin type can occur multiple times for different plugin
   *        implementations.</b>
   * @param pluginType
   *        The plugin type. The plugin type is trusted and must be checked before calling this method.
   * @param lifecycleHook
   *        The lifecycle hook to be called on demand.
   * @param limbusPlugin
   *        The limbus plugin instance. <b>Do not cache the plugin instance outside!</b>
   * @return Returns a proxy interaction object of the desired plugin type to interact with the plugin instance.
   * @throws LimbusClasspathException
   *         Thrown on any exception.
   */
  private <T extends LimbusPlugin> T initializePlugin(String pluginClassName, Class<T> pluginType,
      LimbusLifecycleHook<T> lifecycleHook, T limbusPlugin) throws LimbusClasspathException {
    return initializePlugin(pluginClassName, pluginType, lifecycleHook, limbusPlugin, true);
  }

  /**
   * This method processes a plugin instance and manages caching and creates a proxy for the plugin instance.
   * This method takes care of creating a strong reference to the plugin wich is assumed to be the only strong reference
   * to this plugin instance. Then a proxy object is created to interact with the plugin instance. The proxy object
   * itself only holds a weak reference to the plugin. After this the plugin is initialized and the plugin type is added
   * to the local plugin cache.
   *
   * @param The
   *        classname of the requested plugin. <b>Important: The classname of the plugin is the identifying element
   *        for an instance. This ensures that the plugin type can occur multiple times for different plugin
   *        implementations.</b>
   * @param pluginType
   *        The plugin type. The plugin type is trusted and must be checked before calling this method.
   * @param lifecycleHook
   *        The lifecycle hook to be called on demand.
   * @param limbusPlugin
   *        The limbus plugin instance. <b>Do not cache the plugin instance outside!</b>
   * @param initialize If <code>true</code> the plugin is initialized, if <code>false</code> the caller is responsible
   *        to initialize the plugin using a {@link LimbusContextAction}.
   * @return Returns a proxy interaction object of the desired plugin type to interact with the plugin instance.
   * @throws LimbusClasspathException
   *         Thrown on any exception.
   */
  private <T extends LimbusPlugin> T initializePlugin(String pluginClassName, Class<T> pluginType,
      LimbusLifecycleHook<T> lifecycleHook, T limbusPlugin, boolean initialize) throws LimbusClasspathException {
    // schuettec - 27.01.2017 : Safe the strong reference to the plugin object. This should be the only point in
    // the whole Limbus Engine that is able to hold a strong reference to a plugin.
    createStrongReference(limbusPlugin);
    T interactionProxy = createInitializableProxy(pluginType, limbusPlugin, lifecycleHook);
    if (initialize) {
      _initializePlugin(interactionProxy);
    }
    cachePlugin(pluginClassName, interactionProxy);
    // Add to multicaster
    lifecycleMulticaster.addSubscriber(limbusPlugin);
    return interactionProxy;
  }

  // schuettec - 27.01.2017 : Safe the strong reference to the plugin object. This should be the only point in
  // the whole Limbus Engine that is able to hold a strong reference to a plugin.
  private <T extends LimbusPlugin> void createStrongReference(T createPlugin) {
    strongReferences.add(createPlugin);
  }

  /**
   * Creates a proxy instance for the specified plugin object using the plugin interface.
   *
   * @param pluginInterface
   *        The plugin interface. <b>The object specified here is trusted to be an interface that extends
   *        {@link LimbusPlugin}.</b>
   * @param pluginObject
   *        The plugin instance. <b>The object specified here is trusted to be uninitialized.</b>
   * @return Returns the plugin object proxy.
   */
  @SuppressWarnings("unchecked")
  private <T extends LimbusPlugin> T createInitializableProxy(Class<T> pluginInterface, T pluginObject,
      LimbusLifecycleHook<T> lifecycleHook) {
    // schuettec - 27.01.2017 : Let the proxy be defined by the Limbus Engine's classloader.
    LifecycleProxyHandler<T> handler = new LifecycleProxyHandler<T>(createWeakContext(), pluginObject, lifecycleHook);
    return (T) Proxy.newProxyInstance(Deployment.class.getClassLoader(), new Class<?>[] {
        pluginInterface
    }, handler);
  }

  private void denyNotALimbusPluginInterface(Class<?> expectedType) throws LimbusClasspathException {
    if (expectedType.isInterface()) {
      if (!LimbusPlugin.class.isAssignableFrom(expectedType)) {
        throw new LimbusClasspathException("A Limbus plugin interface must extend LimbusPlugin.");
      }
    } else {
      throw new LimbusClasspathException("A Limbus plugin can only be requested by it's plugin interface.");
    }

  }

  private <T extends LimbusPlugin> T getPluginFromRegistry(String classname, Class<T> expectedType)
      throws LimbusClasspathException {
    LimbusPlugin plugin = pluginRegistry.get(classname);
    try {
      return ReflectionUtil.getAsExpectedType(plugin, expectedType);
    } catch (IllegalTypeException e) {
      throw new LimbusClasspathException(
          String.format("Requested plugin does not have the expected type.\nExpected type: %s\nActual type:%s",
              expectedType.getName(), plugin.getClass()
                  .getName()));
    }
  }

  private void _initializePlugin(LimbusPlugin limbusPlugin) throws LimbusClasspathException {
    limbusContext.doContextAction(new LimbusContextAction<Void, LimbusClasspathException>() {
      @Override
      public Void doAction() throws LimbusClasspathException {
        // Initialize all plugins of this classpath
        try {
          limbusPlugin.initialize();
        } catch (Throwable e) {
          log.error(String.format("Error while initializing limbus plugin %s.", limbusPlugin.getClass()
              .getName()), e);
          throw new LimbusClasspathException("Error while initializing deployment - see log output for stacktrace.");
        }
        return null;
      }
    });
  }

  private <T extends LimbusPlugin> T createPlugin(String classname, Class<T> expectedType)
      throws LimbusClasspathException {
    // schuettec - 06.10.2016 : Class loading without running plugin code, no LimbusContextAction needed.
    denyClassNotFound(getClassloader(), classname);
    // schuettec - 06.10.2016 : Class loading without running plugin code, no LimbusContextAction needed.
    boolean isPlugin = isLimbusPlugin(getClassloader(), classname);
    if (isPlugin) {
      // schuettec - 06.10.2016 : LimbusContextAction is used internally in the following method.
      LimbusPlugin limbusPlugin = getLimbusPlugin(classname);
      return ReflectionUtil.getAsExpectedType(limbusPlugin, expectedType);
    } else {
      throw new LimbusClasspathException(String.format("The class %s is not a Limbus plugin.", classname));
    }
  }

  private <T extends LimbusPlugin> void cachePlugin(String pluginClassName, LimbusPlugin plugin) {
    // Add to registry
    pluginRegistry.put(pluginClassName, plugin);
  }

  private LimbusPlugin getLimbusPlugin(final String className) throws LimbusClasspathException {
    Lang.denyNull("Context", limbusContext);
    Lang.denyNull("Classname", className);

    return limbusContext.doContextAction(new LimbusContextAction<LimbusPlugin, LimbusClasspathException>() {
      @Override
      public LimbusPlugin doAction() throws LimbusClasspathException {
        try {
          Class<?> limbusPluginClass = limbusContext.getClassloader()
              .loadClass(className);
          LimbusPlugin limbusPlugin = (LimbusPlugin) limbusPluginClass.newInstance();
          return limbusPlugin;
        } catch (Throwable e) {
          throw new LimbusClasspathException(String.format("Cannot load Limbus plugin %s", className), e);
        }
      }

    });
  }

  URLClassLoader getClassloader() {
    return limbusContext.getClassloader();
  }

  Classpath getClasspath() {
    return classpath;
  }

  @Override
  public void performInitialize() throws LimbusClasspathException {
  }

  boolean hasPlugins() {
    return !pluginRegistry.isEmpty();
  }

  @Override
  public void performFinish() {
    try {
      // Shutdown sequence: Multicast finish event
      limbusContext.doContextAction(new LimbusContextAction<Void, RuntimeException>() {
        @Override
        public Void doAction() throws RuntimeException {
          lifecycleMulticaster.multicastSilently()
              .finish();
          return null;
        }
      });

    } catch (Throwable e) {
      log.error("Error while finishing deployment.", e);
    } finally {
      // Remove from registry
      this.pluginRegistry.clear();
      this.pluginRegistry = null;

      // Clear subscriber references
      lifecycleMulticaster.clear();
      lifecycleMulticaster = null;

      // Clear strong references
      strongReferences.clear();
      strongReferences = null;

      // Null out references in limbusContext
      limbusContext.finish();
      limbusContext = null;
    }
  }

  /**
   * @return Returns the {@link LimbusContext} for this deployment.
   */
  public LimbusContext getLimbusContext() {
    // schuettec - 30.01.2017 : This method returns the public version of the limbus context not holding strong
    // references.
    return createWeakContext();
  }

  /**
   * @return Creates and returns a new {@link LimbusContext} not holding strong references to Limbus context.
   */
  private LimbusContextPublic createWeakContext() {
    return new LimbusContextPublic(limbusContext);
  }
}
