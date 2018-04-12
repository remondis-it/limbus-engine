package com.remondis.limbus.monitoring;

import static com.remondis.limbus.utils.ReflectionUtil.getAllInterfaces;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class Conventions {

  public static final String PROPERTY_CONFIG_URL = "com.remondis.limbus.monitoring.configURL";
  public static final String DEFAULT_CONFIG_CLASSPATH = "monitoring.xml";

  public static boolean isValidPublisherInterface(Class<?> type) {
    return isInterface(type) && hasTypeAnnotation(type) && onlyHasVoidMethods(type);
  }

  public static void denyInvalidPublisherInterface(Class<?> type) {
    if (!isValidPublisherInterface(type)) {
      throw new IllegalArgumentException(
          String.format("The specified type '%s' is not a valid publisher.", type.getName()));
    }
  }

  public static boolean isValidPublisherImplementation(Class<?> type) {
    return implementsAtLeastOnePublisherInterface(type);
  }

  public static void denyInvalidPublisherImplementation(Class<?> type) {
    if (!isValidPublisherImplementation(type)) {
      throw new IllegalArgumentException(
          String.format("The specified type '%s' is not a valid publisher implementation.", type.getName()));
    }
  }

  private static boolean implementsAtLeastOnePublisherInterface(Class<?> type) {
    return getPublisherInterfacesForImplementation(type).size() > 0;
  }

  public static List<Class<?>> getPublisherInterfacesForImplementation(Class<?> implementation) {
    List<Class<?>> allInterfaces = getAllInterfaces(implementation);
    List<Class<?>> toReturn = new LinkedList<>(allInterfaces);
    Iterator<Class<?>> it = allInterfaces.iterator();
    while (it.hasNext()) {
      Class<?> pubInterface = it.next();
      if (!isValidPublisherInterface(pubInterface)) {
        toReturn.remove(pubInterface);
      }
    }
    if (toReturn.isEmpty()) {
      throw InvalidPublisherException.noPublisherInterfaces(implementation);
    } else {
      return toReturn;
    }
  }

  private static boolean hasTypeAnnotation(Class<?> type) {
    return type.isAnnotationPresent(Publisher.class);
  }

  private static boolean isInterface(Class<?> type) {
    return type.isInterface();
  }

  /**
   * Checks if the specified method is annotated as to be called immediately.
   *
   * @param m
   *        The method to check
   * @return Returns <code>true</code> if the specified method is annotated as to be called immediately,
   *         <code>false</code> otherwise.
   *
   */
  protected static boolean isCallImmediatelyMethod(Method m) {
    boolean isCallImmediately = m.isAnnotationPresent(CallImmediately.class);
    return isCallImmediately;
  }

  private static boolean onlyHasVoidMethods(Class<?> type) {
    Method[] methods = type.getMethods();
    for (Method m : methods) {
      if (m.getReturnType() != Void.TYPE) {
        return false;
      }
    }
    return true;
  }

}
