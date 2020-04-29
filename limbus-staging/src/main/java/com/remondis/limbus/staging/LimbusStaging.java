package com.remondis.limbus.staging;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.util.HashSet;
import java.util.List;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.UUID;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.engine.api.DefaultComponents;
import com.remondis.limbus.staging.staging.Handler;
import com.remondis.limbus.system.ApplicationBuilder;
import com.remondis.limbus.system.LimbusSystemException;
import com.remondis.limbus.system.SystemConfiguration;

/**
 * This is the entry point to build a Limbus Staging environment. Use the {@link #create()} or {@link #create(String)}
 * to create a new Limbus Staging. The provided instance methods are used to describe the deployment, the necessary
 * dependencies, runtime permissions for the deployment classpath as well as the Limbus System components. The latter
 * are needed because a Limbus Staging environment always uses the {@link SystemEngine} to create an environment managed
 * by {@link LimbusSystem}.
 *
 * <p>
 * Please follow the method and class documentations to get further information on how to use each individual feature.
 * </p>
 *
 * @author schuettec
 *
 */
public final class LimbusStaging {

  private LimbusStage stage;

  class ConfigurationHolder<T extends IInitializable<?>, I extends T> {
    private Class<T> requestType;
    private Class<I> componentType;

    ConfigurationHolder(Class<? extends IInitializable<?>> requestType, Class<?> componentType) {
      super();
      this.requestType = (Class<T>) requestType;
      this.componentType = (Class<I>) componentType;
    }

    Class<T> getRequestType() {
      return requestType;
    }

    Class<I> getComponentType() {
      return componentType;
    }

  }

  private LimbusStaging(String deployName) {
    this.stage = new LimbusStage();
    this.stage.setDeployName(deployName);
  }

  /**
   * @param deployName
   *        An optional deploy name.
   * @return Creates and returns a new {@link LimbusStaging} object.
   */
  public static LimbusStaging create(String deployName) {
    return new LimbusStaging(deployName);
  }

  /**
   * @return Creates and returns a new {@link LimbusStaging} object using an empty name for the deployment..
   */
  public static LimbusStaging create() {
    return new LimbusStaging(UUID.randomUUID()
        .toString());
  }

  /**
   * Adds the dependencies defined in the project's POM to the deployment. <b>Only call this method if you have
   * dependencies defined in the project's POM.</b>
   *
   * @return Return this instance for method chaining.
   */
  public LimbusStaging fromProjectDependencies() {
    //@formatter:off
    this.stage.setDependencies(Maven.configureResolver()
                                    .withClassPathResolution(true)
                                    .loadPomFromFile("pom.xml")
                                    .importCompileAndRuntimeDependencies()
                                    .resolve()
                                    .withTransitivity()
                                    .asList(JavaArchive.class));
    //@formatter:on
    return this;
  }

  /**
   * Adds all resources delivered by the {@link CodeSource} of the specified classpath member.
   * <p>
   * Example: If the classpath
   * member is contained in the local project, the whole code source with all of its resources and classes is added to
   * the deployment.
   * </p>
   * <p>
   * <b>Note: Classes and resources with a test scope (or any other scope different) may be included into the deployment
   * if your
   * IDE or build system produces a common classpath for this scopes. Otherwise (and this normally should happen) only
   * the classes and resources of the project's compile dependencies are included into the deployment.</b>
   * </p>
   *
   * @param someClasspathMember
   *        Some class that is contained in the local project in compile scope.
   * @return Returns this object for method chaining.
   * @throws LimbusStagingException
   *         Thrown if the classpath could not be analyzed.
   */
  public LimbusStaging andProjectProviding(Class<?> someClasspathMember) throws LimbusStagingException {
    try {
      JavaArchive deployment = getOrCreateDeployment();
      CodeSource codeSource = someClasspathMember.getProtectionDomain()
          .getCodeSource();
      URL location = codeSource.getLocation();
      if (location.getProtocol()
          .equals("file")) {
        File file = new File(location.toURI());
        boolean directory = file.isDirectory();
        if (directory) {
          File[] resources = file.listFiles();
          for (File resource : resources) {
            deployment.addAsResource(resource);
          }
        } else {
          throw new LimbusStagingException(
              "The code source of the specified classpath member returned a single file. The classpath is corrupt.");
        }
      }
      return this;
    } catch (Exception e) {
      throw new LimbusStagingException("Cannot enumerate project resources due to an exception.", e);
    }
  }

  /**
   * Use this method to generate a dump of all dependencies of the current deployment.
   *
   * @return Returns this object for method chainging.
   */
  public LimbusStaging dumpDeploymentDependencies() {
    List<JavaArchive> dependencies = stage.getDependencies();
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
   * Use this method to generate a dump of all files contained in the current deployment archive that is deployed to the
   * Limbus engine.
   *
   * @return Returns this object for method chainging.
   */
  public LimbusStaging dumpDeploymentContent() {
    JavaArchive deployment = getOrCreateDeployment();
    System.out.println("Printing out the deployment archive content:");
    deployment.writeTo(System.out, Formatters.VERBOSE);
    System.out.println();
    return this;
  }

  /**
   * Use this method to generate a dump of all files contained in the deployment's dependencies. The content of each
   * archive that is deployed to the Limbus Engine as a dependency of the plugin deployment is printed.
   *
   * @return Returns this object for method chainging.
   */
  public LimbusStaging dumpDeploymentDependenciesContent() {
    List<JavaArchive> dependencies = stage.getDependencies();
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

  /**
   * Adds the specified classes to the Limbus plugin deployment.
   *
   * @param classes
   *        The classes to add to the classpath.
   * @return Returns this {@link LimbusStaging} for method chainging.
   */
  public LimbusStaging andClasses(Class<?>... classes) {
    getOrCreateDeployment().addClasses(classes);
    return this;
  }

  private JavaArchive getOrCreateDeployment() {
    JavaArchive deployment = this.stage.getDeployment();
    if (deployment == null) {
      deployment = ShrinkWrap.create(JavaArchive.class);
    }
    this.stage.setDeployment(deployment);
    return deployment;
  }

  /**
   * Grants a permission for the classpath of the deployment.
   *
   * @param permission
   *        The permission to grant for the classpath.
   * @return Returns this object for method chainging.
   */
  public LimbusStaging grantPermission(Permission permission) {
    stage.addPermission(permission);
    return this;
  }

  /**
   * Grants a set of default permission for the classpath of the deployment.
   *
   * @return Returns this object for method chainging.
   */
  public LimbusStaging grantDefaultPermissions() {
    stage.addAllPermission(getDefaultPermissions());
    return this;
  }

  /**
   * @return Returns the {@link Set} of default permissions.
   */
  public static Set<Permission> getDefaultPermissions() {
    Set<Permission> defaultPermissions = new HashSet<>();
    defaultPermissions.add(new PropertyPermission("*", "read"));
    defaultPermissions.add(new RuntimePermission("accessClassInPackage.sun.util.logging.resources"));
    return defaultPermissions;
  }

  /**
   * Applies a set of default components to this {@link LimbusStaging}. The most Limbus Engine implementations deliver
   * an implementation of {@link DefaultComponents} to provide a minimal system configuration.
   *
   * @param defaultComponentsEnumerator
   *        The enumerator of default components. This object should be delivered by Limbus Engine implementations.
   * @return Returns the {@link LimbusSystem} represented by {@link MockLimbusSystem} that allows to add mocked system
   *         components as well as real system components.
   */
  public LimbusSystemStaging withDefaultLimbusComponents(DefaultComponents defaultComponentsEnumerator) {
    LimbusSystemStaging limbusSystemStaging = new LimbusSystemStaging(stage);
    defaultComponentsEnumerator.applyDefaultComponents(limbusSystemStaging);

    return limbusSystemStaging;
  }

  /**
   * @return Returns the {@link LimbusSystem} represented by {@link MockLimbusSystem} that allows to add mocked system
   *         components as well as real system components.
   */
  public LimbusSystemStaging withComponents() {
    return new LimbusSystemStaging(stage);
  }

  /**
   * Creates a {@link LimbusSystemStaging} based on the configuration represented by the specified application class.
   * <b>Note:</b> Be sure to override the component configurations with respective mocks.
   *
   * @param applicationClass The application class defining the component configuration.
   * @return Returns a new {@link LimbusStaging} for further component configuration.
   * @throws LimbusSystemException Thrown if the application class cannot be analyzed.
   */
  public LimbusSystemStaging withComponentsFromApplication(Class<?> applicationClass) throws LimbusSystemException {
    requireNonNull(applicationClass, "Application class must not be null!");
    SystemConfiguration systemConfiguration = ApplicationBuilder
        .buildConfigurationFromApplicationClass(applicationClass);
    LimbusSystemStaging staging = new LimbusSystemStaging(stage);
    systemConfiguration.getComponents()
        .stream()
        .forEach(conf -> {
          Class<? extends IInitializable<?>> componentType = conf.getComponentType();
          if (conf.isPublicComponent()) {
            Class<? extends IInitializable<?>> requestType = conf.getRequestType();
            ConfigurationHolder<IInitializable<?>, IInitializable<?>> configurationHolder = new ConfigurationHolder<>(
                requestType, componentType);
            staging.addComponentConfiguration(configurationHolder.getRequestType(),
                configurationHolder.getComponentType());
          } else {
            staging.addComponentConfiguration(componentType, conf.isFailOnError());
          }
        });
    return staging;
  }

  /**
   * Prepares the environment by registering a new URL stream handler that delivers deployment resources. Use
   * {@link #resetEnvironment()} to undo this operation.
   */
  public static void prepareEnvironment() {
    Handler.installURLStreamHandler();
  }

  /**
   * Resets the environment changes by deregistering an URL stream handler that delivers deployment resources.
   */
  public static void resetEnvironment() {
    Handler.deinstallURLStreamHandler();
  }

}
