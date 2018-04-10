package org.max5.limbus.launcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.max5.limbus.Classpath;
import org.max5.limbus.IInitializable;
import org.max5.limbus.LimbusEngine;
import org.max5.limbus.exceptions.LimbusException;
import org.max5.limbus.launcher.staging.Handler;
import org.max5.limbus.system.LimbusSystem;
import org.max5.limbus.utils.Lang;

/**
 * The {@link LimbusStage} is a configuration holder for a Limbus environment that is ready to get bootstrapped. The
 * public methods can be used to control the lifecycle of the Limbus environment and to access components of the
 * {@link LimbusSystem} managing the specified components.
 *
 * @author schuettec
 *
 */
public final class LimbusStage {

  private static Semaphore stageRunning = new Semaphore(1);

  private boolean hasAccess = false;

  private JavaArchive deployment;
  private List<JavaArchive> dependencies;
  private LimbusSystem system;

  private String deployName;
  private Set<Permission> permissions;

  private Classpath classpath;

  LimbusStage() {
    this.dependencies = new LinkedList<>();
    this.permissions = new HashSet<>();
  }

  /**
   * Configures the {@link EngineLauncher} to start an embedded Limbus Environment. This instance of {@link LimbusStage}
   * needs to acquire exclusive access to the {@link EngineLauncher}.
   *
   * @throws Exception
   *         Thrown on any bootstrapping error or if no exclusive access could be acquired.
   */
  public void startStage() throws Exception {
    hasAccess = stageRunning.tryAcquire();
    denyNoAccess();
    EngineLauncher.skipSystemExit = true;
    try {
      EngineLauncher.bootstrapLimbusSystem(system);
      deploy();
    } catch (Exception e) {
      stopStage();
      throw e;
    }
  }

  /**
   * @return Returns the deployName of the resulting plugin deployment.
   */
  public String getDeployName() {
    return deployName;
  }

  /**
   * Returns the classpath that is available after starting this {@link LimbusStage}.
   *
   * <p>
   * <b>Only available after starting the {@link LimbusStage} using {@link #startStage()}.
   * </p>
   *
   * @return Returns the classpath of the deployment.
   */
  public Classpath getClasspath() {
    return classpath;
  }

  /**
   * Provides access to the specified public Limbus components managed by the running {@link LimbusSystem}.
   *
   * <p>
   * <b>Only available after starting the {@link LimbusStage} using {@link #startStage()}.
   * </p>
   *
   * @param requestType
   *        The request type of the public component to access.
   * @return Returns the component instance.
   * @see org.max5.limbus.system.LimbusSystem#getComponent(java.lang.Class)
   */
  public <T extends IInitializable<?>> T getComponent(Class<T> requestType) {
    return system.getComponent(requestType);
  }

  /**
   * Checks if the specified public Limbus component is available in the running system.
   *
   * <p>
   * <b>Only available after starting the {@link LimbusStage} using {@link #startStage()}.
   * </p>
   *
   * @param requestType
   *        The request type of the public component to check availability for.
   * @return Returns <code>true</code> if the specified public component is accessible, otherwise <code>false</code> is
   *         returned.
   * @see org.max5.limbus.system.LimbusSystem#hasComponent(java.lang.Class)
   */
  public <T extends IInitializable<?>> boolean hasComponent(Class<T> requestType) {
    return system.hasComponent(requestType);
  }

  private void deploy() throws LimbusException, IOException {

    LimbusEngine limbusEngine = system.getComponent(LimbusEngine.class);
    List<URL> urls = new LinkedList<>();
    List<JavaArchive> archives = new LinkedList<>(dependencies);
    for (JavaArchive a : archives) {
      addJavaArchiveAsURL(urls, a, null);
    }
    addJavaArchiveAsURL(urls, deployment, null);
    this.classpath = Classpath.create(deployName)
        .add(urls);
    limbusEngine.deployPlugin(classpath, permissions);
  }

  private void addJavaArchiveAsURL(List<URL> urls, JavaArchive a, String archiveName)
      throws IOException, MalformedURLException {
    byte[] resource = archiveToBytes(a);
    String jarURLStr = null;
    if (Lang.isEmpty(archiveName)) {
      jarURLStr = createJarURL(a);
    } else {
      jarURLStr = createJarURL(archiveName);
    }
    URL jarURL = new URL(jarURLStr);// , resourceHandler);
    urls.add(jarURL);
    addResourceToStagingHandler(resource, jarURL);
  }

  private void addResourceToStagingHandler(byte[] resource, URL jarURL) {
    if (Handler.CURRENT_INSTANCE == null) {
      throw new IllegalStateException(
          "The staging resource URL handler is not available. Call LimbusStaging.prepareEnvironment() before using LimbusStage.");
    } else {
      Handler.CURRENT_INSTANCE.addResource(jarURL, resource);
    }
  }

  private String createJarURL(String archiveName) {
    return String.format("staging:/%s.jar", archiveName);
  }

  private String createJarURL(JavaArchive jar) {
    return String.format("staging:/%s", jar.getName());
  }

  private byte[] archiveToBytes(JavaArchive a) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    a.as(ZipExporter.class)
        .exportTo(bout);
    byte[] byteArray = bout.toByteArray();
    bout.close();
    return byteArray;
  }

  /**
   * Stops the embedded Limbus Environment if started.
   *
   * @return Returns <code>true</code> if the engine terminated cleanly. If there were abandoned threads that did not
   *         terminate on shutdown signal, <code>false</code> is returned.
   */
  public boolean stopStage() {
    try {
      EngineLauncher.shutdownEngine();
      EngineLauncher.waitForShutdown();
      return !EngineLauncher.lastShutdownWasDirty;
    } finally {
      EngineLauncher.skipSystemExit = false;
      hasAccess = false;
      stageRunning.release();
    }
  }

  private void denyNoAccess() {
    if (!hasAccess) {
      throw new IllegalStateException(
          "Concurrent staging: This instance of Limbus Stage currently has no exclusive access to Engine Launcher.");
    }
  }

  void setDeployment(JavaArchive deployment) {
    this.deployment = deployment;
  }

  JavaArchive getDeployment() {
    return deployment;
  }

  void setDependencies(List<JavaArchive> dependencies) {
    this.dependencies = dependencies;
  }

  void setSystem(LimbusSystem system) {
    this.system = system;
  }

  void setDeployName(String deployName) {
    this.deployName = deployName;
  }

  /**
   * Adds the specified permission to the deployment's classpath.
   *
   * <p>
   * <b>This method only has an effect if the {@link LimbusStage} is started after calling this method.
   * </p>
   *
   * @param permission
   *        The permission to add.
   */
  public void addPermission(Permission permission) {
    this.permissions.add(permission);
  }

  /**
   * Adds the specified set of {@link Permission}s to the deployment's classpath. If permissions were already defines,
   * the specified set is added on top.
   * <p>
   * <b>This method only has an effect if the {@link LimbusStage} is started after calling this method.
   * </p>
   *
   * @param permissions
   *        The set of permissions to add.
   */
  public void addAllPermission(Set<Permission> permissions) {
    this.permissions.addAll(permissions);
  }

  /**
   * @return Returns a new list of archives that are currently part of the deployment.
   */
  public List<JavaArchive> getDependencies() {
    return new LinkedList<>(dependencies);
  }

}
