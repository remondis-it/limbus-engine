package com.remondis.limbus.staging;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.api.LimbusException;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.launcher.EngineLauncher;
import com.remondis.limbus.system.LimbusSystem;

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

  private LimbusSystem system;

  private List<JavaArchive> dependencies;

  LimbusStage(LimbusSystem limbusSystem) {
    this.system = limbusSystem;
    this.dependencies = new LinkedList<>();
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
    } catch (Exception e) {
      stopStage();
      throw e;
    }
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
   * @see com.remondis.limbus.system.LimbusSystem#getComponent(java.lang.Class)
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
   * @see com.remondis.limbus.system.LimbusSystem#hasComponent(java.lang.Class)
   */
  public <T extends IInitializable<?>> boolean hasComponent(Class<T> requestType) {
    return system.hasComponent(requestType);
  }

  public void deploy(LimbusStagingDeployment deployment) throws LimbusException, IOException {
    requireNonNull(deployment, "Deploytment must not be null!");
    LimbusEngine limbusEngine = system.getComponent(LimbusEngine.class);
    limbusEngine.deployPlugin(deployment.getClasspath(), deployment.getPermissions());
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

  /**
   * Adds the dependencies defined in the project's POM to the deployment. <b>Only call this method if you have
   * dependencies defined in the project's POM.</b>
   *
   * @return Return this instance for method chaining.
   */
  public LimbusStage fromProjectDependencies() {
    //@formatter:off
    this.dependencies = Maven.configureResolver()
                                    .withClassPathResolution(true)
                                    .loadPomFromFile("pom.xml")
                                    .importCompileAndRuntimeDependencies()
                                    .resolve()
                                    .withTransitivity()
                                    .asList(JavaArchive.class);
    //@formatter:on
    return this;
  }

  /**
   * Use this method to generate a dump of all dependencies of the current deployment.
   *
   * @return Returns this object for method chainging.
   */
  public LimbusStage dumpDeploymentDependencies() {
    if (dependencies.isEmpty()) {
      System.out.println("No deployment dependencies are defined yet.");
    } else {
      System.out.println("Printing out the deployment dependencies:");
      for (JavaArchive d : dependencies) {
        System.out.printf("- Dependency %s\n", d.getName());
      }
    }
    return this;
  }

  /**
   * Use this method to generate a dump of all files contained in the deployment's dependencies. The content of each
   * archive that is deployed to the Limbus Engine as a dependency of the plugin deployment is printed.
   *
   * @return Returns this object for method chainging.
   */
  public LimbusStage dumpDeploymentDependenciesContent() {
    if (dependencies.isEmpty()) {
      System.out.println("No deployment dependencies are defined yet.");
    } else {
      System.out.println("Printing out the deployment dependencies archive content:");
      for (JavaArchive d : dependencies) {
        System.out.printf("Printing out the archive content of: %s\n", d.getName());
        d.writeTo(System.out, Formatters.VERBOSE);
      }
    }
    return this;
  }
}
