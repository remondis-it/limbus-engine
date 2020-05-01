package com.remondis.limbus.staging;

import static java.util.Objects.requireNonNull;

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

  static class ConfigurationHolder<T extends IInitializable<?>, I extends T> {
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

  private LimbusStaging() {
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
  public static LimbusSystemStaging fromDefaultLimbusComponents(DefaultComponents defaultComponentsEnumerator) {
    LimbusSystemStaging limbusSystemStaging = new LimbusSystemStaging();
    defaultComponentsEnumerator.applyDefaultComponents(limbusSystemStaging);
    return limbusSystemStaging;
  }

  /**
   * @return Returns the {@link LimbusSystem} represented by {@link MockLimbusSystem} that allows to add mocked system
   *         components as well as real system components.
   */
  public static LimbusSystemStaging fromComponents() {
    return new LimbusSystemStaging();
  }

  /**
   * Creates a {@link LimbusSystemStaging} based on the configuration represented by the specified application class.
   * <b>Note:</b> Be sure to override the component configurations with respective mocks.
   *
   * @param applicationClass The application class defining the component configuration.
   * @return Returns a new {@link LimbusStaging} for further component configuration.
   * @throws LimbusSystemException Thrown if the application class cannot be analyzed.
   */
  public static LimbusSystemStaging fromComponentsFromApplication(Class<?> applicationClass)
      throws LimbusSystemException {
    requireNonNull(applicationClass, "Application class must not be null!");
    SystemConfiguration systemConfiguration = ApplicationBuilder
        .buildConfigurationFromApplicationClass(applicationClass);
    LimbusSystemStaging staging = new LimbusSystemStaging();
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
