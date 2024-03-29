package com.remondis.limbus.engine.api;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.security.Permission;
import java.util.Set;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.api.LimbusException;
import com.remondis.limbus.api.LimbusPlugin;

/**
 * This is the service interface for the Limbus Container. This service allows access to the container for deploying and
 * undeploying of plugins.
 *
 * @author schuettec
 *
 */
public interface LimbusEngine extends IInitializable<Exception> {

  /**
   * Subscribes the specified {@link DeploymentListener} to the event sysasdasdasdasdtem of this {@link LimbusEngine}.
   *
   * @param listener
   *        The subscriber.
   */
  public void addDeploymentListener(DeploymentListener listener);

  /**
   * Unsubscribes the specified {@link DeploymentListener} from the event system of this {@link LimbusEngine}.
   *
   * @param listener
   *        The listener to unsubscribe.
   */
  public void removeDeploymentListener(DeploymentListener listener);

  /**
   * Returns the {@link Classpath} by deploy name, if such a classpath is currently deployed on the
   * {@link LimbusEngine}
   *
   * @param deployName
   *        The deployment name
   * @return Returns the {@link Classpath} deployed using the specified deployname if exists, otherwise
   *         <code>null</code> is returned.
   * @throws NoSuchDeploymentException
   *         Thrown if no classpath for the specified deployment name is deployed on this container.
   */
  public Classpath getClasspath(String deployName) throws NoSuchDeploymentException;

  /**
   * Checks if a deployment is currently deployed on this container.
   *
   * <p>
   * <b>Note: This method does not provide reliable results so that a classpath is available for later use. After
   * checking the availability using this method, the classpath may be undeployed by another thread.</b>
   * </p>
   *
   * @param deployName
   *        The deployname to check.
   * @return Returns <code>true</code> if the classpath is currently deployed, <code>false</code> otherwise.
   */
  public boolean hasClasspath(String deployName);

  /**
   * Records the std/out and std/err output of a plugin's thread with the specified {@link ByteArrayOutputStream}s.
   * Use {@link ByteArrayOutputStream}s to be able to retrieve the data. <b>Note: There must be an open channel for
   * the specified classloader context.</b>
   *
   * @param classpath
   *        The classpath to identify std/out/err access.
   * @param thread
   *        The thread to identify std/out/err access.
   * @param stdOutTarget
   *        The delegate recording stream for std/out
   * @param stdErrTarget
   *        The delegate recording stream for std/err
   * @throws NoSuchDeploymentException
   *         Thrown if the classpath is not deployed on this container.
   */
  public void recordChannel(Classpath classpath, Thread thread, ByteArrayOutputStream stdOutTarget,
      ByteArrayOutputStream stdErrTarget) throws NoSuchDeploymentException;

  /**
   * Checks if there is already a recording running for the specified classloader and thread.
   *
   * @param classpath
   *        The classpath
   * @param thread
   *        The thread
   * @return Returns <code>true</code> if the std/out/err channels are already recorded, <code>false</code> otherwise.
   * @throws NoSuchDeploymentException
   *         Thrown if the classpath is not deployed on this container.
   */
  public boolean isRecordingChannel(Classpath classpath, Thread thread) throws NoSuchDeploymentException;

  /**
   * Stops recording of the specified classloader and thread context.
   *
   * @param classpath
   *        The classpath
   * @param thread
   *        The thread
   * @return Returns an array containing the std/out delegate stream as first element and the std/err delegate stream
   *         as second element. To retrieve the data the stream will be returned as {@link ByteArrayOutputStream}.
   *         <b>Note: Close the streams after receiving them through this method.</b>
   * @throws NoSuchDeploymentException
   *         Thrown if the classpath is not deployed on this container.
   */
  public ByteArrayOutputStream[] stopRecordChannel(Classpath classpath, Thread thread) throws NoSuchDeploymentException;

  /**
   * Provides access to deployed plugins of this {@link LimbusEngine}.
   * <p>
   * The plugin instances returned by this method are wrapped in a proxy class. Any call to the plugin interface will
   * check that the plugin is available before the real call is performed. This ensures that the plugin may be
   * undeployed at any time and no direct references to the plugin's instance can be cached.</b>
   * Plus that, any call to a plugin instance is performed within the scope of the respective {@link LimbusContext}.
   * </p>
   *
   * @param classpath
   *        The classpath the plugin is expected to be available in.
   * @param classname
   *        The classname of the plugin.
   * @param expectedType
   *        The expected plugin type. The plugin type must be derived from {@link LimbusPlugin}.
   * @return Returns the plugin if available.
   * @throws LimbusException
   *         Thrown if the plugin could not be initialized, the plugin was not found, the classpath is not
   *         deployed or the expected type does not match.
   * @throws NoSuchDeploymentException
   *         Thrown if the classpath is not deployed on this container.
   */
  public default <T extends LimbusPlugin> T getPlugin(Classpath classpath, String classname, Class<T> expectedType)
      throws LimbusException, NoSuchDeploymentException {
    return getPlugin(classpath, classname, expectedType, null);
  }

  /**
   * Provides access to deployed plugins of this {@link LimbusEngine}.
   * <p>
   * The plugin instances returned by this method are wrapped in a proxy class. Any call to the plugin interface will
   * check that the plugin is available before the real call is performed. This ensures that the plugin may be
   * undeployed at any time and no direct references to the plugin's instance can be cached.</b>
   * Plus that, any call to a plugin instance is performed within the scope of the respective {@link LimbusContext}.
   * </p>
   *
   * @param classpath
   *        The classpath the plugin is expected to be available in.
   * @param classname
   *        The classname of the plugin.
   * @param expectedType
   *        The expected plugin type. The plugin type must be derived from {@link LimbusPlugin}.
   * @param lifecycleHook
   *        (Optional) The lifecycle hook to be executed. <b>The lifecycle hook specified here will only be set on
   *        first plugin request.</b>
   * @return Returns the plugin if available.
   * @throws LimbusException
   *         Thrown if the plugin could not be initialized, the plugin was not found, the classpath is not
   *         deployed or the expected type does not match.
   * @throws NoSuchDeploymentException
   *         Thrown if the classpath is not deployed on this container.
   */
  public default <T extends LimbusPlugin> T getPlugin(Classpath classpath, String classname, Class<T> expectedType,
      LimbusLifecycleHook<T> lifecycleHook) throws LimbusException, NoSuchDeploymentException {
    return getPlugin(classpath, classname, expectedType, lifecycleHook, true);
  }

  /**
   * Provides access to deployed plugins of this {@link LimbusEngine}.
   * <p>
   * The plugin instances returned by this method are wrapped in a proxy class. Any call to the plugin interface will
   * check that the plugin is available before the real call is performed. This ensures that the plugin may be
   * undeployed at any time and no direct references to the plugin's instance can be cached.</b>
   * Plus that, any call to a plugin instance is performed within the scope of the respective {@link LimbusContext}.
   * </p>
   *
   * @param classpath
   *        The classpath the plugin is expected to be available in.
   * @param classname
   *        The classname of the plugin.
   * @param expectedType
   *        The expected plugin type. The plugin type must be derived from {@link LimbusPlugin}.
   * @param lifecycleHook
   *        (Optional) The lifecycle hook to be executed. <b>The lifecycle hook specified here will only be set on
   *        first plugin request.</b>
   * @param initialize If <code>true</code> the plugin is initialized, if <code>false</code> the caller is responsible
   *        to initialize the plugin using a {@link LimbusContextAction}.
   * @return Returns the plugin if available.
   * @throws LimbusException
   *         Thrown if the plugin could not be initialized, the plugin was not found, the classpath is not
   *         deployed or the expected type does not match.
   * @throws NoSuchDeploymentException
   *         Thrown if the classpath is not deployed on this container.
   */
  public <T extends LimbusPlugin> T getPlugin(Classpath classpath, String classname, Class<T> expectedType,
      LimbusLifecycleHook<T> lifecycleHook, boolean initialize) throws LimbusException, NoSuchDeploymentException;

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
  public <T extends LimbusPlugin, S extends T> S getPluginAsInterface(Classpath classpath, String classname,
      Class<T> pluginInterface, Class<S>[] supportedIntefaces, ClassLoader toDefineIn,
      LimbusLifecycleHook<T> lifecycleHook, boolean initialize) throws LimbusException;

  /**
   * Performs a reflective plugin method invocation. This way, plugin methods can be called, that are not part of a
   * plugin interface. Use this method to call anonymous methods in a plugin. <b>Note: The method invocation is
   * performed
   * within a {@link LimbusContextAction}</b>
   * 
   * @param <T> The plugin type.
   * @param plugin The plugin instance.
   * @param name The method name.
   * @param parameterTypes The array of parameter types to resolve the method.
   * @param parameters The actual parameter values to call the method.
   * @return Returns the return value of the method invocation.
   */
  @SuppressWarnings("rawtypes")
  public <T extends LimbusPlugin> InvocationResult invokePluginMethodReflectively(Classpath classpath, String classname,
      Class<T> expectedType, LimbusLifecycleHook<T> lifecycleHook, boolean initialize, String name,
      Class[] parameterTypes, Object[] parameters) throws LimbusException, NoSuchDeploymentException;

  /**
   * Since the {@link LimbusEngine} only exposes proxy objects for plugin instances, Java Bean property introspection is
   * not possible without unwrapping the plugin type. This method makes sure that the plugin type is introspected
   * safely.
   * 
   * @param <T> The plugin type.
   * @param field The field to inject.
   * @param pluginInstance The plugin instance (normally a proxy object).
   * @param value The value to inject.
   */
  public <T extends LimbusPlugin> void performPropertyInjection(Field field, T pluginInstance, Object value);

  /**
   * Deploys a classpath on this Limbus container and restricts the classpath using the specified set of permissions.
   * <p>
   * <b>Note:</b> If the specified classpath already exists on this container, this method does nothing. <b>This
   * method cannot be used to change permissions on a classpath.</b>
   * </p>
   *
   * @param classpath
   *        The classpath to deploy.
   * @param permissions
   *        The set of permissions to be granted to the classes of this classpath.
   * @throws LimbusException
   *         Thrown if the deployment failed.
   */
  public void deployPlugin(Classpath classpath, Set<Permission> permissions) throws LimbusException;

  /**
   * Redeploys a classpath on this Limbus container to change the permissions on a classpath.
   *
   * @param classpath
   *        The classpath to redeploy.
   * @param permissions
   *        The new set of permissions to be granted to the classes of this classpath.
   * @throws LimbusException
   *         Thrown if the redeployment failed.
   * @throws UndeployVetoException
   *         Thrown if a component vetoed the undeploy operation.
   */
  public void redeployPlugin(Classpath classpath, Set<Permission> permissions)
      throws LimbusException, UndeployVetoException;

  /**
   * Undeploys the specified classpath from this Limbus container and finishes all its plugins and services.
   *
   * <p>
   * <b>Note: If the classpath was already undeployed or is not available on this container, this method does
   * nothing.</b>
   * </p>
   *
   * @param classPath
   *        The classpath to be undeployed.
   * @throws UndeployVetoException
   *         Thrown if a component vetoed the undeploy operation.
   */
  public void undeployPlugin(Classpath classPath) throws UndeployVetoException;

  /**
   * @return Returns the description of the currently deployed shared classpath.
   */
  public Classpath getSharedClasspath();

  /**
   * @return Returns the class loader that holds classes shared across all plugin classpaths.
   */
  public ClassLoader getSharedClassLoader();

  /**
   * @return Returns all deployed plugin classpaths.
   */
  public Set<Classpath> getPluginClasspaths();

  /**
   * Returns the {@link LimbusContext} for the specified classpath. This object is needed to perform operations on
   * plugin objects.
   *
   * @param classpath
   *        The classpath to get the {@link LimbusContext} for. Must be deployed on the current
   *        {@link LimbusEngine}.
   * @return Returns the {@link LimbusContext} for the specified deployed classpath.
   * @throws NoSuchDeploymentException
   *         Thrown if this classpath is not deployed on this container.
   */
  public LimbusContext getLimbusContext(Classpath classpath) throws NoSuchDeploymentException;

  /**
   * @return Returns the Maven version of this Limbus Engine.
   */
  public String getEngineVersion();

  /**
   * @return Returns the Maven group id of this Limbus Engine.
   */
  public String getEngineGroupId();

  /**
   * @return Returns the Maven artifact id of this Limbus Engine.
   */
  public String getEngineArtifactId();
}
