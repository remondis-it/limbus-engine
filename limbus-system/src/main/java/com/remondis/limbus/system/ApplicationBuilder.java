package com.remondis.limbus.system;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.HashSet;
//import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.remondis.limbus.utils.LambdaException;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * Internal class to build a {@link LimbusSystem} from a {@link LimbusApplication} configuration.
 * 
 * @author schuettec
 *
 */
public class ApplicationBuilder {

  /**
   * Builds a {@link SystemConfiguration} to be used to create a {@link LimbusSystem} or a staging environment.
   * 
   * @param applicationClass The application class that defines the application configuration.
   * @return Returns a {@link SystemConfiguration} reflecting the component configurations.
   * @throws LimbusSystemException Thrown on any error.
   */
  public static SystemConfiguration buildConfigurationFromApplicationClass(Class<?> applicationClass)
      throws LimbusSystemException {
    requireNonNull(applicationClass, "application class must not be null!");

    LimbusApplication application = applicationClass.getDeclaredAnnotation(LimbusApplication.class);
    ObjectFactory objectFactory = createObjectFactory(application);
    SystemConfiguration configuration = new SystemConfiguration();
    configuration.setObjectFactory(objectFactory);

    if (nonNull(application)) {
      importBundles(applicationClass, configuration);
    }

    addComponentConfigurationFromPackage(applicationClass, configuration);

    getComponentConfigurationFromAnnotations(applicationClass).stream()
        .forEach(compConf -> {
          if (compConf.isPublicComponent()) {
            if (configuration.hasPrivateComponent(compConf.getComponentType())) {
              configuration.removePrivateComponent(compConf.getComponentType());
            }
            configuration.addComponentConfiguration(compConf);
          }
        });
    return configuration;
  }

  private static void addComponentConfigurationFromPackage(Class<?> applicationClass, SystemConfiguration configuration)
      throws LimbusSystemException {
    String packageName = applicationClass.getPackage()
        .getName();
    try {
      List<String> classNamesFromPackage = ReflectionUtil.getClassNamesFromPackage(packageName);
      Set<Class> classes = loadComponentClasses(classNamesFromPackage);

      Set<Class> requestTypes = new HashSet<>();
      requestTypes.addAll(configuration.getKnownRequestTypes());
      requestTypes.addAll(classes.stream()
          .filter(filterInterface())
          .collect(Collectors.toSet()));

      // Minimize the amount of classes to be analyzed
      classes.removeAll(requestTypes);

      // add public components and collect the private components.
      classes.stream()
          // Class has to implement one of the request types
          .filter(cls -> {
            Optional<Class> requestType = requestTypes.stream()
                .filter(superType -> superType.isAssignableFrom(cls))
                .findFirst();
            if (requestType.isPresent()) {
              // TODO: We should support failOnError. Maybe with @LimbusComponent(failOnError=...) on components.
              configuration.addComponentConfiguration(new ComponentConfiguration(requestType.get(), cls, true));
              return false;
            } else {
              return true;
            }
          })
          .forEach(cls ->
      // TODO: We should support failOnError. Maybe with @LimbusComponent(failOnError=...) on components.
      configuration.addComponentConfiguration(new ComponentConfiguration(cls, true)));

    } catch (Exception e) {
      throw new LimbusSystemException("Cannot determine classes for package " + packageName, e);
    }
  }

  private static Set<Class> loadComponentClasses(List<String> classNamesFromPackage) throws Exception {
    try {
      return classNamesFromPackage.stream()
          .map(clsName -> {
            try {
              return Class.forName(clsName, false, Thread.currentThread()
                  .getContextClassLoader());
            } catch (ClassNotFoundException e) {
              throw LambdaException.of(e);
            }
          })
          .filter(filterLimbusComponentAnnotation())
          .collect(Collectors.toSet());

    } catch (LambdaException e) {
      throw e.getCause();
    }
  }

  private static ObjectFactory createObjectFactory(LimbusApplication application) throws LimbusSystemException {
    Class<? extends ObjectFactory> objectFactoryType = application.objectFactory();
    try {
      return ReflectionUtil.newInstance(ObjectFactory.class, objectFactoryType);
    } catch (Exception e) {
      throw new LimbusSystemException("Could not create object factory: " + objectFactoryType.getName(), e);
    }
  }

  private static void importBundles(Class<?> applicationClass, SystemConfiguration configuration) {
    Arrays.stream(applicationClass.getAnnotationsByType(ImportBundle.class))
        .forEach(importBundle -> {
          Arrays.stream(importBundle.value())
              .map(ApplicationBuilder::getComponentConfigurationFromAnnotations)
              .flatMap(List::stream)
              .forEach(configuration::addComponentConfiguration);
        });
  }

  private static List<ComponentConfiguration> getComponentConfigurationFromAnnotations(Class<?> type) {
    List<ComponentConfiguration> confs = new LinkedList<>();
    Arrays.stream(type.getAnnotationsByType(PrivateComponent.class))
        .forEach(privateComponent -> confs.add(getConfigurationComponentFromPrivateAnnotation(privateComponent)));
    Arrays.stream(type.getAnnotationsByType(PublicComponent.class))
        .forEach(publicComponent -> confs.add(getConfigurationComponentFromPublicAnnotation(publicComponent)));
    return confs;
  }

  @SuppressWarnings("unchecked")
  private static ComponentConfiguration getConfigurationComponentFromPublicAnnotation(PublicComponent publicComponent) {
    Class requestType = publicComponent.requestType();
    return new ComponentConfiguration(requestType, publicComponent.type(), publicComponent.failOnError());
  }

  private static ComponentConfiguration getConfigurationComponentFromPrivateAnnotation(
      PrivateComponent privateComponent) {
    return new ComponentConfiguration(privateComponent.value(), privateComponent.failOnError());
  }

  private static Predicate<? super Class> filterLimbusComponentAnnotation() {
    return cls -> nonNull(cls.getAnnotation(LimbusComponent.class));
  }

  private static Predicate<? super Class> filterInterface() {
    return Class::isInterface;
  }
}
