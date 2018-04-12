package com.remondis.limbus.monitoring;

import static com.remondis.limbus.monitoring.Conventions.DEFAULT_CONFIG_CLASSPATH;
import static com.remondis.limbus.monitoring.Conventions.PROPERTY_CONFIG_URL;
import static com.remondis.limbus.utils.ReflectionUtil.getClassLoader;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.XStreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MonitoringFactory {

  private static final Logger log = LoggerFactory.getLogger(MonitoringFactory.class);

  protected static Class<?>[] annotatedClasses = new Class<?>[] {
      MonitoringConfiguration.class, Pattern.class, PublisherConfig.class, ProcessingConfig.class
  };

  private static ReentrantLock initLock = new ReentrantLock();

  private static volatile InitState state = InitState.CLEAR;
  private static MonitoringRegistry registry;

  private static MonitoringProcessor processor;

  /**
   * @return Returns <code>true</code> if the monitoring is currently active and processing monitoring information.
   *         Otherwise <code>false</code> is returned.
   */
  public static boolean isActive() {
    return perform();
  }

  /**
   * Returns the monitoring object for the specified name.
   *
   * @param name
   *        The name of the client to be monitored.
   * @return Returns the {@link Monitoring} object that can be used to publish monitoring records.
   */
  public static Monitoring getMonitoring(String name) {
    Client client = _createMonitoring(name);
    return client;
  }

  /**
   * Returns the monitoring object for the specified class.
   *
   * @param forClass
   *        The class to create a monitoring object for.
   * @return Returns the {@link Monitoring} object that can be used to publish monitoring records.
   */
  public static Monitoring getMonitoring(Class<?> forClass) {
    return getMonitoring(forClass.getName());
  }

  /**
   * Finishes the monitoring and disposes all resources, configurations and processing threads.
   *
   * @param awaitTermination
   *        If <code>true</code> this method waits until the monitoring events currently known are published, if
   *        <code>false</code> the currently known monitoring events are dropped.
   *
   */
  public static void shutdown(boolean awaitTermination) {
    _noop();
    initLock.lock();
    if (processor != null) {
      processor.shutdown(awaitTermination);
      processor = null;
    }
    if (registry != null) {
      registry.clear();
      registry = null;
    }
    state = InitState.CLEAR;
    initLock.unlock();
  }

  /**
   *
   * Finishes the monitoring immediately and disposes all resources, configurations and processing threads. <b>Note:
   * This method will wait until the last monitoring records will be send to publishers before terminating.</b> Use
   * {@link #shutdown(boolean)} for other options.
   */
  public static void shutdown() {
    shutdown(true);
  }

  /**
   * This is a non-blocking enqueue method to enqueue a monitoring record. This record will be consumed asynchronous by
   * publishers.
   *
   * @param call
   *        The recorded method call to enqueue.
   */
  protected static void enqueueRecord(MethodCall call) {
    if (perform()) {
      processor.submitRecord(call);
    } else {
      System.out.println("Monitoring call was not submitted! State was " + state);
    }
  }

  private static Client _createMonitoring(String name) {
    // schuettec - 12.04.2017 : Check initialization
    initializeOnDemand();

    // schuettec - 12.04.2017 : Always create new client objects.
    Client client = new Client(name);
    return client;
  }

  /**
   * Configures the monitoring framework using the specified {@link URL}.
   *
   * @param configurationURL
   *        The {@link URL} pointing to a valid XML configuration.
   * @throws IllegalStateException
   *         Thrown if the monitoring was already configured. Use {@link #shutdown()} to re-initialize it.
   */
  public static void configureMonitoring(URL configurationURL) {
    initLock.lock();
    try {
      if (state == InitState.CLEAR) {
        try {
          MonitoringConfiguration configuration = loadConfiguration(configurationURL);
          if (configuration == null) {
            log.debug("No monitoring configuration was configured. Disabling monitoring.");
            _noop();
          } else {
            registry = new MonitoringRegistry(configuration);
            processor = new MonitoringProcessor(configuration);
            _initialized();
          }
        } catch (Exception e) {
          log.error("Cannot initialize monitoring framework.", e);
          _noop();
        }
      } else {
        throw new IllegalStateException("The monitoring was already configured. Use shutdown before re-configuring.");
      }
    } finally {
      initLock.unlock();
    }
  }

  private static void initializeOnDemand() {
    initLock.lock();
    try {
      if (state == InitState.CLEAR) {
        MonitoringConfiguration configuration = loadConfiguration();
        if (configuration == null) {
          log.debug("No monitoring configuration was configured. Disabling monitoring.");
          _noop();
        } else {
          registry = new MonitoringRegistry(configuration);
          processor = new MonitoringProcessor(configuration);
          _initialized();
        }
      }
    } catch (Exception e) {
      log.error("Cannot initialize monitoring framework.", e);
      _noop();
    } finally {
      initLock.unlock();
    }
  }

  /**
   * @return Returns <code>true</code> if the monitoring system performs real event processing. If the monitoring is in
   *         some other state, <code>false</code> is returned to indicate a no-op behaviour for {@link Client}
   *         instances.
   */
  protected static boolean perform() {
    return state == InitState.INITIALIZED;
  }

  private static void _noop() {
    initLock.lock();
    state = InitState.NOOP;
    initLock.unlock();
  }

  private static void _initialized() {
    initLock.lock();
    state = InitState.INITIALIZED;
    initLock.unlock();
  }

  /**
   * @return Returns the the state of the monitoring.
   */
  protected static InitState getState() {
    return state;
  }

  /**
   * @return Returns the {@link XStreamUtil} object that can be used to write or read monitoring configuration objects.
   */
  protected static XStreamUtil getDefaultXStream() {
    return new XStreamUtil(annotatedClasses);
  }

  protected static MonitoringConfiguration loadConfiguration() {
    String property = System.getProperty(PROPERTY_CONFIG_URL);
    if (property == null) {
      log.debug(
          "No monitoring configuration was specified by system properties. Searching default configuration on classpath.");
      return loadConfiguration(DEFAULT_CONFIG_CLASSPATH);
    } else {
      try {
        URL url = new URL(property);
        MonitoringConfiguration configuration = loadConfiguration(url);
        log.debug(
            "Monitoring configuration was loaded from URL specified by system properties. Using configuration: '{}'",
            url.toString());
        return configuration;
      } catch (MalformedURLException e) {
        throw InvalidConfigurationException.malformedURL(property, e);
      }

    }
  }

  protected static MonitoringConfiguration loadConfiguration(String classpathName)
      throws InvalidConfigurationException {
    ClassLoader classLoader = getClassLoader(MonitoringFactory.class);
    URL resourceURL = classLoader.getResource(classpathName);
    if (resourceURL == null) {
      logNoConfigurationFound();
      return null;
    } else {
      return loadConfiguration(resourceURL);
    }

  }

  private static void logNoConfigurationFound() {
    if (log.isDebugEnabled()) {
      log.debug("The default monitoring configuration '{}' was not found.", Conventions.DEFAULT_CONFIG_CLASSPATH);
    }
  }

  protected static MonitoringConfiguration loadConfiguration(URL url) throws InvalidConfigurationException {
    Lang.denyNull("url", url);
    XStreamUtil xstream = getDefaultXStream();
    try {
      InputStream openStream = url.openStream();
      return xstream.readObject(MonitoringConfiguration.class, openStream);
    } catch (Exception e) {
      throw InvalidConfigurationException.readError(url.toExternalForm(), e);
    }
  }

  /**
   * Logs a dump of the current monitoring configuration.
   */
  public static void logMonitoringConfiguration() {
    log.info("Logging monitoring configuration:\n{}", registry);
  }

  /**
   * This method returns the set of configured publishers for the specified pattern and publisher interface. The pattern
   * is evaluated against the list of known patterns starting with the most specific pattern. The first matching and
   * most specific pattern will be chosen.
   *
   * @param clientName
   *        The name of the client publishing records. Configured patterns are evaluated against this name to search
   *        publishers for this client.
   * @param publisherInterface
   *        The specified publisher type.
   * @return Returns the set of configured publisher instances or an empty set if no publisher was found.
   */
  protected static Set<Object> getPublishers(String clientName, Class<?> publisherInterface) {
    return registry.getPublishers(clientName, publisherInterface);
  }

  /**
   * This method returns the set of configured publishers for the specified pattern and publisher interface. The pattern
   * is evaluated against the list of known patterns starting with the most specific pattern. The first matching and
   * most specific pattern will be chosen.
   *
   * @param clientClass
   *        The class of the client publishing records. Configured patterns are evaluated against this name to search
   *        publishers for this client.
   * @param publisherInterface
   *        The specified publisher type.
   * @return Returns the set of configured publisher instances or an empty set if no publisher was found.
   */
  protected static Set<Object> getPublishers(Class<?> clientClass, Class<?> publisherInterface) {
    if (perform()) {
      return registry.getPublishers(clientClass.getName(), publisherInterface);
    } else {
      return Collections.emptySet();
    }
  }

}
