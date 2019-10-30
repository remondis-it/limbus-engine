package com.remondis.limbus.monitoring;

import static com.remondis.limbus.monitoring.Conventions.denyInvalidPublisherImplementation;
import static com.remondis.limbus.monitoring.Conventions.isValidPublisherInterface;
import static com.remondis.limbus.monitoring.InvalidConfigurationException.duplicatePublisher;
import static com.remondis.limbus.monitoring.InvalidConfigurationException.duplicatePublisherId;
import static com.remondis.limbus.utils.ReflectionUtil.getAllInterfaces;
import static com.remondis.limbus.utils.ReflectionUtil.getClassLoader;
import static com.remondis.limbus.utils.ReflectionUtil.newInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.utils.ObjectCreateException;

/**
 * This class defines the monitoring registry. It maps the configured monitoring client patterns to available
 * publisher interfaces. The available interfaces are mapped to the set of configured publisher implementations.
 *
 * @author schuettec
 *
 */
/**
 * @author schuettec
 *
 */
public class MonitoringRegistry {

  private static final Logger log = LoggerFactory.getLogger(MonitoringRegistry.class);

  /**
   * Holds the patterns with specifity descending.
   */
  protected List<Pattern> orderedPatterns;

  protected List<IInitializable<?>> lifecycleObjects;

  protected Map<Pattern, Map<Class<?>, Set<Object>>> patternToPublisher;

  public MonitoringRegistry(MonitoringConfiguration configuration)
      throws InvalidConfigurationException, SecurityException, ObjectCreateException {
    super();
    this.lifecycleObjects = new LinkedList<IInitializable<?>>();
    this.patternToPublisher = createPatternToPublisherMapping(configuration);
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
  public Set<Object> getPublishers(String clientName, Class<?> publisherInterface) {

    Pattern pattern = getFirstMatchingPattern(clientName);

    if (pattern == null) {
      if (log.isTraceEnabled()) {
        log.trace("No matching pattern found for client '{}'.", clientName);
      }
      return Collections.emptySet();
    }

    if (patternToPublisher.containsKey(pattern)) {
      Map<Class<?>, Set<Object>> interfaceToPublishersMap = patternToPublisher.get(pattern);
      if (interfaceToPublishersMap.containsKey(publisherInterface)) {
        return interfaceToPublishersMap.get(publisherInterface);
      } else {
        if (log.isTraceEnabled()) {
          log.trace("The specified publisher interface '{}' was not found - requesting client was '{}'.",
              publisherInterface.getName(), clientName);
        }
        return Collections.emptySet();
      }
    } else {
      if (log.isTraceEnabled()) {
        log.trace("The matching pattern '{}' is not known by the publisher mapping - this is an implementation fault.",
            pattern.getPattern());
      }
      return Collections.emptySet();
    }
  }

  private Pattern getFirstMatchingPattern(String clientName) {
    for (Pattern p : orderedPatterns) {
      String pattern = p.getPattern();
      if (clientName.startsWith(pattern)) {
        return p;
      }
    }
    return null;
  }

  protected List<Pattern> orderPatternsSpecifityDescending(List<Pattern> pattern) {
    Collections.sort(pattern, Collections.reverseOrder(new PatternSpecifityComparator()));
    return pattern;
  }

  protected Object createPublisher(String clazz) throws ObjectCreateException, SecurityException {
    try {
      ClassLoader classLoader = getClassLoader(MonitoringFactory.class);
      Class<?> publisherImpl = classLoader.loadClass(clazz);
      denyInvalidPublisherImplementation(publisherImpl);
      Object newInstance = newInstance(publisherImpl);
      return newInstance;
    } catch (SecurityException e) {
      throw e;
    } catch (ClassNotFoundException e) {
      throw new ObjectCreateException("Cannot create publisher instance - class was not found.", e);
    }
  }

  protected Map<Pattern, Map<Class<?>, Set<Object>>> createPatternToPublisherMapping(MonitoringConfiguration config)
      throws InvalidConfigurationException, ObjectCreateException, SecurityException {

    Map<Pattern, Map<Class<?>, Set<Object>>> patternToPublishers = new ConcurrentHashMap<Pattern, Map<Class<?>, Set<Object>>>();

    Map<String, Object> idToPublisher = createIdToPublisherMapping(config);
    List<Pattern> patterns = config.getPatterns();
    denyDuplicatePatterns(patterns);
    orderedPatterns = orderPatternsSpecifityDescending(patterns);

    // Now we can iterate over the patterns starting with the most specific pattern.
    for (Pattern p : orderedPatterns) {
      List<String> publisherIDs = p.getPublishers();
      for (String pubId : publisherIDs) {
        // For every publisher id, get the publisher and its corresponding publisher interface
        denyUnknownPublisherId(idToPublisher, pubId);
        Object object = idToPublisher.get(pubId);
        Set<Class<?>> publisherInterfaces = getPublisherInterfacesForImplementation(object.getClass());
        for (Class<?> pubInterface : publisherInterfaces) {
          addPatternToPublishersMapping(patternToPublishers, p, pubInterface, object);
        }
      }
    }
    return patternToPublishers;
  }

  protected void addPatternToPublishersMapping(Map<Pattern, Map<Class<?>, Set<Object>>> patternToPublishers, Pattern p,
      Class<?> pubInterface, Object object) {
    Map<Class<?>, Set<Object>> interfaceToPublishers = getOrCreateInterfacesToPublishersMap(patternToPublishers, p);
    Set<Object> publishers = getOrCreatePublishersSet(interfaceToPublishers, pubInterface);
    publishers.add(object);
  }

  protected Set<Object> getOrCreatePublishersSet(Map<Class<?>, Set<Object>> interfaceToPublishers,
      Class<?> pubInterface) {
    if (interfaceToPublishers.containsKey(pubInterface)) {
      return interfaceToPublishers.get(pubInterface);
    } else {
      KeySetView<Object, Boolean> newKeySet = ConcurrentHashMap.newKeySet();
      interfaceToPublishers.put(pubInterface, newKeySet);
      return newKeySet;
    }
  }

  protected Map<Class<?>, Set<Object>> getOrCreateInterfacesToPublishersMap(
      Map<Pattern, Map<Class<?>, Set<Object>>> patternToPublishers, Pattern p) {
    if (patternToPublishers.containsKey(p)) {
      return patternToPublishers.get(p);
    } else {
      ConcurrentHashMap<Class<?>, Set<Object>> map = new ConcurrentHashMap<Class<?>, Set<Object>>();
      patternToPublishers.put(p, map);
      return map;
    }
  }

  protected void denyUnknownPublisherId(Map<String, Object> idToPublisher, String publisherId)
      throws InvalidConfigurationException {
    if (!idToPublisher.containsKey(publisherId)) {
      throw InvalidConfigurationException.unknownPublisherId(publisherId);
    }
  }

  protected void denyDuplicatePatterns(List<Pattern> patterns) throws InvalidConfigurationException {
    HashSet<Pattern> cleaned = new HashSet<>(patterns);
    if (cleaned.size() != patterns.size()) {
      List<Pattern> delta = new LinkedList<>(patterns);
      delta.removeAll(cleaned);
      throw InvalidConfigurationException.duplicatePatterns(delta);
    }
  }

  protected Set<Class<?>> getPublisherInterfacesForImplementation(Class<?> implementation) {
    List<Class<?>> allInterfaces = getAllInterfaces(implementation);
    Set<Class<?>> validInterfaces = new HashSet<Class<?>>();
    Iterator<Class<?>> it = allInterfaces.iterator();
    while (it.hasNext()) {
      Class<?> pubInterface = it.next();
      if (isValidPublisherInterface(pubInterface)) {
        validInterfaces.add(pubInterface);
      }
    }
    if (allInterfaces.isEmpty()) {
      throw InvalidPublisherException.noPublisherInterfaces(implementation);
    } else {
      return validInterfaces;
    }
  }

  protected String monitoringConfigurationAsString() {
    StringBuilder b = new StringBuilder();

    String format = "%-40s | %-40s | %-40s";
    b.append(String.format(format, "Pattern", "Publisher Interface", "Publisher Implementation"))
        .append("\n");
    Iterator<Pattern> it = patternToPublisher.keySet()
        .iterator();
    while (it.hasNext()) {
      Pattern p = it.next();
      String pattern = shortString(p.getPattern());
      Map<Class<?>, Set<Object>> interfaceToImpls = patternToPublisher.get(p);
      Iterator<Class<?>> interfaces = interfaceToImpls.keySet()
          .iterator();
      while (interfaces.hasNext()) {
        Class<?> pubInt = interfaces.next();
        Set<Object> impls = interfaceToImpls.get(pubInt);
        for (Object impl : impls) {
          String interfaceName = shortString(pubInt.getName());
          String implName = shortString(impl.getClass()
              .getName());
          b.append(String.format(format, pattern, interfaceName, implName))
              .append("\n");
          pattern = "";
        }
      }
    }
    return b.toString();
  }

  protected String shortString(String string) {
    return shortString(40, string);
  }

  protected String shortString(int maxChars, String string) {
    String shortener = "...";
    String newString = string;
    int actual = newString.length();
    if (actual > maxChars) {
      int diff = (actual - maxChars) + shortener.length();
      newString = shortener + newString.substring(diff, actual);
    }
    return newString;
  }

  /**
   * Creates the configured publishers.
   *
   * @param config
   *        The configuration.
   * @return Returns the mapping of declared publisher id to publisher instance.
   * @throws InvalidConfigurationException
   *         Thrown if a duplicate publisher id was detected.
   * @throws ObjectCreateException
   *         Thrown if a publisher could not be created.
   * @throws SecurityException
   *         Thrown on missing permissions.
   */
  protected Map<String, Object> createIdToPublisherMapping(MonitoringConfiguration config)
      throws InvalidConfigurationException, SecurityException, ObjectCreateException {
    Map<String, Object> mapping = new HashMap<>();
    Set<String> addedPublisherClassNames = new HashSet<>();
    List<PublisherConfig> publishers = config.getPublishers();
    Iterator<PublisherConfig> it = publishers.iterator();
    while (it.hasNext()) {
      PublisherConfig p = it.next();
      String id = p.getId();
      Object instance = p.getInstance();
      Class<?> clazz = instance.getClass();
      if (mapping.containsKey(id)) {
        throw duplicatePublisherId(id);
      } else if (addedPublisherClassNames.contains(clazz.getName())) {
        throw duplicatePublisher(clazz);
      } else {
        initializeOnDemand(instance);
        mapping.put(id, instance);
        addedPublisherClassNames.add(clazz.getName());
      }
    }

    return mapping;
  }

  /**
   * If the publisher instance is an {@link IInitializable} call the initialize lifecycle method.
   *
   * @param instance
   *        The instance to initialize.
   */
  private void initializeOnDemand(Object instance) throws ObjectCreateException {
    if (instance instanceof IInitializable<?>) {
      IInitializable<?> iInitializable = (IInitializable<?>) instance;
      try {
        iInitializable.initialize();
        lifecycleObjects.add(iInitializable);
      } catch (Exception e) {
        throw new ObjectCreateException(String
            .format("Could not initialized IInitializable publisher implementation '%s'.", iInitializable.getClass()
                .getName()),
            e);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return monitoringConfigurationAsString();
  }

  public void clear() {
    finishPublishers();
    this.lifecycleObjects.clear();
    this.lifecycleObjects = null;
    this.orderedPatterns.clear();
    this.orderedPatterns = null;
    this.patternToPublisher.clear();
    this.patternToPublisher = null;
  }

  private void finishPublishers() {
    Iterator<IInitializable<?>> it = lifecycleObjects.iterator();
    while (it.hasNext()) {
      IInitializable<?> toFinish = it.next();
      try {
        toFinish.finish();
      } catch (Exception e) {
        log.warn("Finishing of an IInitializable was expected to be silent but threw an exception.", e);
      }
    }
  }

}
