package com.remondis.limbus;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.net.URLClassLoader;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.remondis.limbus.events.EventMulticaster;
import com.remondis.limbus.events.EventMulticasterFactory;
import com.remondis.limbus.exceptions.LimbusException;
import com.remondis.limbus.exceptions.NoSuchDeploymentException;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.logging.LogTarget;
import com.remondis.limbus.system.LimbusComponent;
import com.remondis.limbus.utils.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Limbus Engine manages the lifecycle of plugins and provides a class loader hierarchy to isolate plugins
 * with the use of a shared class loader. Plugins can be added and removed during runtime.
 *
 * @author schuettec
 *
 */
public abstract class LimbusEngineImpl extends Initializable<Exception> implements LimbusEngine {

  private static final Logger log = LoggerFactory.getLogger(LimbusEngine.class);

  @LimbusComponent
  private SharedClasspathProvider sharedClassPathProvider;

  @LimbusComponent
  private LimbusFileService filesystem;

  @LimbusComponent
  private LogTarget logTarget;

  private SharedClassLoader sharedClassLoader;

  private Map<Classpath, Deployment> deploymentMap;

  private Map<String, Classpath> deploynames;

  /**
   * This lock is used for synchronization of deployments.
   */
  private ReentrantLock deploymentsLock = new ReentrantLock();

  /**
   * Holds the Limbus reference observer that tries to detect the point in time where a classloader is garbage
   * collected.
   */
  private LimbusReferenceObserver<ClassLoader> referenceObserver;

  /**
   * Holds the {@link DeploymentListener} that subscribed via the service method.
   */
  private EventMulticaster<DeploymentListener> deploymentListeners;

  public LimbusEngineImpl() {
    super();
  }

  @Override
  public void addDeploymentListener(DeploymentListener listener) {
    deploymentListeners.addSubscriber(listener);
  }

  @Override
  public void removeDeploymentListener(DeploymentListener listener) {
    deploymentListeners.removeSubscriber(listener);
  }

  @Override
  public void recordChannel(Classpath classpath, Thread thread, ByteArrayOutputStream stdOutTarget,
      ByteArrayOutputStream stdErrTarget) throws NoSuchDeploymentException {
    URLClassLoader classloader = this.getClassloader(classpath);
    logTarget.recordChannel(classloader, thread, stdOutTarget, stdErrTarget);
  }

  @Override
  public boolean isRecordingChannel(Classpath classpath, Thread thread) throws NoSuchDeploymentException {
    URLClassLoader classLoader = this.getClassloader(classpath);
    return logTarget.isRecordingChannel(classLoader, thread);
  }

  @Override
  public ByteArrayOutputStream[] stopRecordChannel(Classpath classpath, Thread thread)
      throws NoSuchDeploymentException {
    URLClassLoader classLoader = this.getClassloader(classpath);
    return logTarget.stopRecordChannel(classLoader, thread);
  }

  // CONVENTION: The following documentation describes public API conventions:
  /**
   * <p>
   * Public plugin APIs delivering classes used by plugins need to be accessible in the engine's classpath
   * because both
   * the engine and the plugins need to access the same classes. Other classes in the engine's classpath may not be
   * accessible to plugins. Therefore only a few packages are allowed to be accessible in this classpath. This packages
   * are defined by the most common package prefix. Every resource or class request is checked: If it accesses one of
   * the packages a prefix was defined for, access to this classes or resources is granted, otherwise the request is
   * delegated to the parent classloader of the engines classpath (which usually results in skipping the AppClassloader
   * and redirecting to its parent delivering the bootstrap classpath).
   * <p>
   *
   * <p>
   * <b>Note: The convention described above has a side-effect. The public API may not deliver resources in the root
   * package that must be available for plugins. The package prefix would be empty and this would grant access to the
   * whole engine classpath.</b>
   * </p>
   *
   *
   * @return Returns the list of allowed package prefixes to be accessed in the classpath of this engine.
   */
  private final List<String> getAllowedPackagePrefixes() {
    String[] publicAccessPackages = getPublicAccessPackages();
    Lang.denyNull("publicAccessPackages", publicAccessPackages);
    List<String> allowedPackagePrefixes = new LinkedList<String>(Arrays.asList(publicAccessPackages));
    List<String> defaultAllowedPackagePrefixes = LimbusUtil.getDefaultAllowedPackagePrefixes();
    allowedPackagePrefixes.addAll(defaultAllowedPackagePrefixes);
    return allowedPackagePrefixes;
  }

  /**
   * @return Returns the array of package prefixes that are accessible in the engines classpath. This are usually the
   *         common prefixes of packages that deliver the public API for plugins deployable in this engine. <b>May not
   *         return <code>null</code></b>
   */
  protected abstract String[] getPublicAccessPackages();

  @Override
  public LimbusContext getLimbusContext(Classpath classpath) throws NoSuchDeploymentException {
    checkState();
    if (deploymentMap.containsKey(classpath)) {
      Deployment deployContext = deploymentMap.get(classpath);
      return deployContext.getLimbusContext();
    } else {
      throw new NoSuchDeploymentException("The specified classpath is not deployed on this container.");
    }
  }

  /**
   * This method returns the classloader of the specified classpath.
   *
   * @param classpath
   *        The currently deployed classpath
   * @return Returns the corresponding classloader this classpath was loaded from.
   * @throws NoSuchDeploymentException
   *         Thrown if this classpath is not deployed on this container.
   */
  protected URLClassLoader getClassloader(Classpath classpath) throws NoSuchDeploymentException {
    if (deploymentMap.containsKey(classpath)) {
      Deployment deployContext = deploymentMap.get(classpath);
      if (deployContext.getClassloader() != null) {
        return deployContext.getClassloader();
      }
    }
    throw NoSuchDeploymentException.createDefault();
  }

  @Override
  public <T extends LimbusPlugin> T getPlugin(Classpath classpath, String classname, Class<T> expectedType)
      throws LimbusException, NoSuchDeploymentException {
    checkState();
    return getPlugin(classpath, classname, expectedType, null);
  }

  @Override
  public <T extends LimbusPlugin> T getPlugin(Classpath classpath, String classname, Class<T> expectedType,
      LimbusLifecycleHook<T> lifecycleHook) throws LimbusException, NoSuchDeploymentException {
    checkState();
    if (deploymentMap.containsKey(classpath)) {
      Deployment deployment = deploymentMap.get(classpath);
      return deployment.getPlugin(classname, expectedType, lifecycleHook);
    } else {
      throw new NoSuchDeploymentException("The specified classpath is not deployed on this container.");
    }
  }

  @Override
  public Classpath getClasspath(String deployName) throws NoSuchDeploymentException {
    // buschmann - 04.05.2017 : No blocking synchronization to improve performance. Note: This section is threadsafe
    // because a ConcurrentHashMap is used. This results in returning the last seen state.
    Lang.denyNull("deployName", deployName);
    if (deploynames.containsKey(deployName)) {
      return deploynames.get(deployName);
    } else {
      throw new NoSuchDeploymentException(String.format("A deployment with the name %s does not exist.", deployName));
    }
  }

  private void _deployClasspath(Deployment deployment) throws LimbusException {
    URLClassLoader classLoader = deployment.getClassloader();
    // Create a logging environment for this classpath. If this classpath is anonymous (does not have a deploy name),
    // the logging will not be separated.
    Classpath classpath = deployment.getClasspath();
    if (classpath.hasDeployName()) {
      String deployName = classpath.getDeployName();
      logTarget.openChannel(classLoader, deployName);
    }

    try {
      // Initialize deployment will scan for LimbusPlugins and will manage its lifecycles.
      deployment.initialize();
    } catch (Throwable e) { // schuettec - 13.12.2016 : Catch Throwable here, because different Throwables and Errors
                            // may be thrown by some operations (ExceptionInInitializerError, etc.). Those errors must
                            // be handled, too!
      log.error("Error while deploying plugin - printing out exception", e);

      // On error undeploy
      try {
        _undeployClasspath(deployment);
      } catch (Exception suppress) {
        // Undeploy silently.
      }

      if (log.isDebugEnabled()) {
        log.error("Error while deploying limbus plugin with the following classpath.");
        LimbusUtil.logClasspath("Plugin classpath", classpath, log);
      } else {
        log.error("Error while deploying limbus plugin - set log level to debug for more information.");
      }

      throw new LimbusException("Error while deploying a classpath - please see stacktrace on std/err.");
    }
  }

  private void _undeployClasspath(Deployment deployment) {

    // Close the logging environment for this classpath if this classpath is not anonymous
    // Get the classloader before finishing the deployment (this will erase the reference)
    URLClassLoader classloader = deployment.getClassloader();

    // Enqueue the classloader reference to the reference observer
    this.referenceObserver.observeReferenceTo(classloader);

    try {
      // Finish the deploy context and delete references
      deployment.finish();

    } finally {

      // Close logTarget for plugin
      Classpath classpath = deployment.getClasspath();
      if (classpath.hasDeployName()) {
        logTarget.closeChannel(classloader);
      }

      // Try to ad-hoc garbage collect the classloadfer.
      WeakReference<Object> classloaderWeakRef = new WeakReference<Object>(classloader);
      classloader = null;
      boolean garbageCollected = LimbusUtil.isGarbageCollected(classloaderWeakRef);
      if (garbageCollected) {
        log.info("Classloader was unloaded - the ad-hoc garbage collection was successful!");
      } else {
        log.info(
            "The ad-hoc garbage collection was not successful - see log output of LimbusReferenceObserver to get long-term garbage collection notifications.");
      }
    }
  }

  @Override
  protected void performInitialize() throws Exception {
    // schuettec - 05.04.2017 : In earlier versions this was done in Engine Launcher.
    Workarounds.executePreventiveWorkarounds();

    this.deploymentListeners = EventMulticasterFactory.create(DeploymentListener.class);

    this.referenceObserver = new LimbusReferenceObserver<ClassLoader>();
    this.referenceObserver.initialize();

    this.deploymentMap = new ConcurrentHashMap<Classpath, Deployment>();
    this.deploynames = new ConcurrentHashMap<String, Classpath>();

    // Deploy all components from shared classpath
    sharedClassPathProvider.checkClasspath();
    Classpath sharedClasspath = sharedClassPathProvider.getSharedClasspath();
    this.sharedClassLoader = new SharedClassLoader(filesystem, LimbusEngine.class.getClassLoader(),
        getAllowedPackagePrefixes(), sharedClasspath.getClasspath());
    Deployment sharedDeployment = new Deployment(sharedClasspath, sharedClassLoader);
    deploymentMap.put(sharedClasspath, sharedDeployment);
    try {
      _deployClasspath(sharedDeployment);
    } catch (Exception e) {
      // If plugins from shared classpath fail to deploy we are not forced to shutdown the whole engine.
      log.warn("Error while deploying plugins from shared classpath.", e);
    }
  }

  @Override
  protected void performFinish() {
    // Null reference to shared classloader because if held, the classloader leak detection cannot unload the shared
    // classloader.
    this.sharedClassLoader = null;

    // Multicast finish event to all deployed components
    try {
      for (Classpath classpath : deploymentMap.keySet()) {
        undeployPlugin(classpath);
      }
    } catch (Exception e) {
      // We have to skip exceptions because we must not abort the performFinish!
      log.warn("Exception while undeploying classpath.", e);
    }

    // Clear all deploy context
    deploymentMap.clear();
    deploynames.clear();

    if (referenceObserver != null) {
      try {
        this.referenceObserver.finish();
      } catch (Exception e) {
        // Keep this silent
      }
    }

    if (this.deploymentListeners != null) {
      this.deploymentListeners.clear();
      this.deploymentListeners = null;
    }
  }

  @Override
  public boolean hasClasspath(String deployName) {
    Lang.denyNull("deployName", deployName);
    return deploynames.containsKey(deployName);
  }

  @Override
  public void deployPlugin(Classpath classpath, Set<Permission> permissions) throws LimbusException {
    checkState();
    deploymentsLock.lock();
    try {
      _deployPlugin(classpath, permissions);
    } finally {
      deploymentsLock.unlock();
    }
  }

  private void _deployPlugin(Classpath classpath, Set<Permission> permissions) throws LimbusException {
    if (deploymentMap.containsKey(classpath)) {
      return;
    }

    log.info("Deploy process started for plugin classpath.");
    LimbusUtil.logClasspath("plugin", classpath, log);
    LimbusUtil.logPermissions("plugin", permissions, log);

    PluginClassLoader pluginClassLoader = new PluginClassLoader(filesystem, sharedClassLoader,
        classpath.getClasspath());
    pluginClassLoader.setPermissions(permissions);

    Deployment deployment = new Deployment(classpath, pluginClassLoader);
    deploymentMap.put(classpath, deployment);

    _deployClasspath(deployment);

    log.info("Deploy process finished successfully.");

    if (classpath.hasDeployName()) {
      deploynames.put(classpath.getDeployName(), classpath);
    }

    // Notify deployment subscribers
    deploymentListeners.multicastSilently()
        .classpathDeployed(classpath);
  }

  @Override
  public void undeployPlugin(Classpath classpath) throws UndeployVetoException {
    checkState();
    deploymentsLock.lock();

    try {
      _undeployPlugin(classpath);
    } finally {
      deploymentsLock.unlock();
    }
  }

  private void _undeployPlugin(Classpath classpath) throws UndeployVetoException {
    // schuettec - 04.05.2017 : Do nothing if the classpath is not deployed on this container.
    if (!deploymentMap.containsKey(classpath)) {
      return;
    }

    SimpleVeto undeployVeto = new SimpleVeto();

    // Notify deployment subscribers starting undeploy
    deploymentListeners.multicastSilently()
        .classpathUndeploying(classpath, undeployVeto);

    // schuettec - 16.05.2017 : Only perform the undeploy if the operation was not vetoed.
    if (undeployVeto.isConfirmed()) {
      try {
        Deployment deployment = deploymentMap.get(classpath);
        deploymentMap.remove(classpath);

        if (classpath.hasDeployName()) {
          deploynames.remove(classpath.getDeployName());
        }

        if (classpath.hasDeployName()) {
          log.info("Undeploy process started for plugin classpath {}.", classpath.getDeployName());
        } else {
          log.info("Undeploying anonymous classpath");
        }
        LimbusUtil.logClasspath("plugin", classpath, log);
        _undeployClasspath(deployment);
        log.info("Undeploy process finished successfully.");
      } finally {
        // Notify deployment subscribers finishing undeploy
        deploymentListeners.multicastSilently()
            .classpathUndeployed(classpath);
      }

    } else {
      throw UndeployVetoException.newDefault();
    }
  }

  @Override
  public void redeployPlugin(Classpath classpath, Set<Permission> permissions)
      throws LimbusException, UndeployVetoException {
    deploymentsLock.lock();

    try {
      _undeployPlugin(classpath);
      _deployPlugin(classpath, permissions);
    } finally {
      deploymentsLock.unlock();
    }
  }

  @Override
  public Classpath getSharedClasspath() {
    checkState();

    return sharedClassPathProvider.getSharedClasspath();
  }

  @Override
  public Set<Classpath> getPluginClasspaths() {
    checkState();

    Set<Classpath> deployedClasspaths = new HashSet<>();
    // Remove the shared classpath
    Iterator<Classpath> it = deploymentMap.keySet()
        .iterator();
    while (it.hasNext()) {
      Classpath classpath = it.next();
      if (classpath.equals(getSharedClasspath())) {
        continue;
      } else {
        deployedClasspaths.add(classpath.clone());
      }
    }

    return deployedClasspaths;
  }

}
