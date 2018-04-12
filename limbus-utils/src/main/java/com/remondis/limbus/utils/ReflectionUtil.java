package com.remondis.limbus.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is a util class that provides useful reflective methods. <b>Intended for internal use only!</b>.
 *
 * @author schuettec
 *
 */
public class ReflectionUtil {

  private static final Map<Class<?>, Object> DEFAULT_VALUES;

  static {
    // schuettec - 08.02.2017 : According to the spec:
    // https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
    Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
    map.put(boolean.class, false);
    map.put(char.class, '\0');
    map.put(byte.class, (byte) 0);
    map.put(short.class, (short) 0);
    map.put(int.class, 0);
    map.put(long.class, 0L);
    map.put(float.class, 0f);
    map.put(double.class, 0d);
    DEFAULT_VALUES = Collections.unmodifiableMap(map);
  }

  private static final Map<String, Class<?>> primitiveNameMap = new HashMap<>();

  static {
    primitiveNameMap.put(boolean.class.getName(), boolean.class);
    primitiveNameMap.put(byte.class.getName(), byte.class);
    primitiveNameMap.put(char.class.getName(), char.class);
    primitiveNameMap.put(short.class.getName(), short.class);
    primitiveNameMap.put(int.class.getName(), int.class);
    primitiveNameMap.put(long.class.getName(), long.class);
    primitiveNameMap.put(double.class.getName(), double.class);
    primitiveNameMap.put(float.class.getName(), float.class);
    primitiveNameMap.put(void.class.getName(), void.class);
  }

  /**
   * Returns the class representing the specified primitive.
   *
   * @param primitive
   *        The primitive name like "double", "char", "boolean", etc.
   * @return Returns the {@link Class} representing the primitive.
   */
  public static Class<?> primitiveForName(String primitive) {
    if (primitiveNameMap.containsKey(primitive)) {
      return primitiveNameMap.get(primitive);
    } else {
      throw new IllegalArgumentException(
          String.format("Specified primitive name %s is not a Java primitive.", primitive));
    }
  }

  /**
   * Checks if a specified type name refers to a primitive type. The type name of a privimtive type is assumed to be
   * <tt>boolean.class.getName()</tt> for example.
   *
   * @param primitive
   *        The type name as string.
   * @return Returns <code>true</code> if the specified name is a valid primitive type name, <code>false</code>
   *         otherwise.
   */
  public static boolean isPrimitive(String primitive) {
    return primitiveNameMap.containsKey(primitive);
  }

  /**
   * Returns the default value for the specified primitive type according to the Java Language Specification. See
   * https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html for more information.
   *
   * @param type
   *        The type of the primitive.
   * @return Returns the default value of the specified primitive type.
   */
  @SuppressWarnings("unchecked")
  public static <T> T defaultValue(Class<T> type) {
    return (T) DEFAULT_VALUES.get(type);
  }

  /**
   * Checks if the method has a return type.
   *
   * @param method
   *        the method
   * @return <code>true</code>, if return type is not {@link Void} or <code>false</code>
   *         otherwise.
   */
  public static boolean hasReturnType(Method method) {
    return !method.getReturnType()
        .equals(Void.TYPE);
  }

  /**
   * Creates a new instance reflectively.
   *
   * @param implementation
   *        the type to instantiate
   * @return Returns the created object casted to the specified super type.
   * @throws ObjectCreateException
   *         Thrown if the object creation failed.
   * @throws SecurityException
   *         Thrown if the runtime does not grant the permissions to reflectively create an object.
   */
  public static <I> I newInstance(Class<I> implementation) throws ObjectCreateException, SecurityException {
    return newInstance(implementation, implementation);
  }

  /**
   * Creates a new instance reflectively.
   *
   * @param superType
   *        The super type the created instance is returned as.
   * @param implementation
   *        The implementation type to instantiate.
   * @return Returns the created object casted to the specified super type.
   * @throws ObjectCreateException
   *         Thrown if the object creation failed.
   * @throws SecurityException
   *         Thrown if the runtime does not grant the permissions to reflectively create an object.
   */
  public static <T> T newInstance(Class<T> superType, Class<?> implementation)
      throws ObjectCreateException, SecurityException {
    String classname = superType.getName();
    try {
      Constructor<?> constructor = implementation.getConstructor();
      Object newInstance = constructor.newInstance();
      return getAsExpectedType(newInstance, superType);
    } catch (InstantiationException e) {
      throw new ObjectCreateException(String.format("The class %s was expected to be instantiable.", classname));
    } catch (IllegalAccessException e) {
      throw new ObjectCreateException(
          String.format("The constructor of class %s was expected to be public.", classname));
    } catch (IllegalArgumentException e) {
      throw new ObjectCreateException(String
          .format("The constructor of class %s was expected to be a zero argument default constructor", classname));
    } catch (NoSuchMethodException e) {
      throw new ObjectCreateException(String
          .format("The constructor of class %s was expected to be a zero argument default constructor", classname));
    } catch (InvocationTargetException e) {
      Throwable toThrow = e;
      if (e.getCause() != null) {
        toThrow = e.getCause();
      }
      throw new ObjectCreateException("Could not create action object.", toThrow);
    }
  }

  /**
   * Checks the type of the specified object against the expected type. On matching type, the casted object is returned,
   * otherwise an {@link IllegalTypeException} is thrown.
   *
   * @param object
   *        The object to check
   * @param expectedType
   *        The expected type.
   * @return Returns the casted object.
   * @throws IllegalTypeException
   *         Thrown if the object type does not match the expected type. Match means the expected type must be
   *         assignable from the specified object. See {@link Class#isAssignableFrom(Class)} for more information.
   */
  public static <T> T getAsExpectedType(Object object, Class<T> expectedType) throws IllegalTypeException {
    if (object == null || expectedType.isAssignableFrom(object.getClass())) {
      return expectedType.cast(object);
    } else {
      throw new IllegalTypeException(
          String.format("Specified object was expected to be of type %s, but actually is of type %s.",
              expectedType.getName(), object.getClass()
                  .getName()));
    }
  }

  /**
   * Check if the specified type is an interface and if not throws an {@link IllegalTypeException}.
   *
   * @param type
   *        The type.
   * @throws IllegalTypeException
   *         Thrown if the type is not an interface.
   */
  public static void denyNotInterface(Class<?> type) throws IllegalTypeException {
    if (!isInterface(type)) {
      throw new IllegalTypeException(String.format("The specified type must be an interface: %s", type.getName()));
    }
  }

  /**
   * This method loads a service class or interface specified by the classname. This method
   * checks if
   * the class or interface is of a specific super type.
   *
   * @param classToLoad
   *        The classname of the service
   * @param superType
   *        The super type classname
   * @return Returns the loaded service class.
   * @throws Exception
   */
  public static <T> Class<T> loadServiceClass(String classToLoad, Class<T> superType) throws Exception {
    ClassLoader classloader = getClassLoader(superType);
    return loadServiceClass(classToLoad, superType, classloader);
  }

  /**
   * This method loads a service class or interface specified by the classname. This method
   * checks if
   * the class or interface is of a specific super type.
   *
   * @param classToLoad
   *        The classname of the service
   * @param superType
   *        The super type classname
   * @param classloader
   *        Uses this classloader to load the specified class
   * @return Returns the loaded service class.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> loadServiceClass(String classToLoad, Class<T> superType, ClassLoader classloader)
      throws Exception {
    try {
      Class<?> classToCheck = classloader.loadClass(classToLoad);
      if (!superType.isAssignableFrom(classToCheck)) {
        throw new Exception(String.format("The requested type '%s' does not have the expected type '%s'.",
            classToCheck.getName(), superType.getName()));
      } else {
        return (Class<T>) classToCheck;
      }
    } catch (ClassNotFoundException e) {
      throw new Exception(String.format("The specified type '%s' is not available or cannot be loaded.", classToLoad),
          e);
    }
  }

  /**
   * @param caller
   *        The caller of this method. Its classloader will be returned if no thread context classloader is available.
   * @return Returns the context classloader of the current {@link Thread}. If the context classloader is null, the
   *         classloader of this class is returned.
   */
  public static ClassLoader getClassLoader(Class<?> caller) {
    ClassLoader classLoader = Thread.currentThread()
        .getContextClassLoader();
    if (classLoader == null) {
      classLoader = caller.getClassLoader();
    }
    return classLoader;
  }

  /**
   * @param clazz
   *        The class to check
   * @return Returns <code>true</code> if the specified class represents an interface. Otherwise <code>false</code> is
   *         returned.
   *
   */
  public static boolean isInterface(Class<?> clazz) {
    return clazz.isInterface();
  }

  /**
   * Creates a list of all fields in the type hierarchy of the specified class, annotated with the specified
   * annotations.
   *
   * @param clazz
   *        The class to collect fields from.
   * @param annotations
   *        The annotations to check.
   *
   * @return Returns a list of all field in the type hierarchy annotated with the specified annotations.
   */
  @SafeVarargs
  public static List<Field> getAllAnnotatedFields(Class<?> clazz, Class<? extends Annotation>... annotations) {
    List<Field> fields = new LinkedList<Field>();
    if (clazz != Object.class) {
      Class<?> parent = clazz.getSuperclass();
      fields.addAll(getAllAnnotatedFields(parent, annotations));
      for (Field f : clazz.getDeclaredFields()) {
        for (Class<? extends Annotation> annotation : annotations) {
          if (f.isAnnotationPresent(annotation)) {
            fields.add(f);
          }
        }
      }
    }
    return fields;
  }

  /**
   * <p>
   * Gets a {@code List} of all interfaces implemented by the given
   * class and its superclasses.
   * </p>
   *
   * <p>
   * The order is determined by looking through each interface in turn as
   * declared in the source file and following its hierarchy up. Then each
   * superclass is considered in the same way. Later duplicates are ignored,
   * so the order is maintained.
   * </p>
   *
   * @param cls
   *        the class to look up, may be {@code null}
   * @return the {@code List} of interfaces in order,
   *         {@code null} if null input
   */
  public static List<Class<?>> getAllInterfaces(Class<?> cls) {
    if (cls == null) {
      return null;
    }

    HashSet<Class<?>> interfacesFound = new LinkedHashSet<Class<?>>();
    getAllInterfaces(cls, interfacesFound);

    return new ArrayList<Class<?>>(interfacesFound);
  }

  /**
   * Get the interfaces for the specified class.
   *
   * @param cls
   *        the class to look up, may be {@code null}
   * @param interfacesFound
   *        the {@code Set} of interfaces for the class
   */
  private static void getAllInterfaces(Class<?> cls, HashSet<Class<?>> interfacesFound) {
    while (cls != null) {
      Class<?>[] interfaces = cls.getInterfaces();

      for (Class<?> i : interfaces) {
        if (interfacesFound.add(i)) {
          getAllInterfaces(i, interfacesFound);
        }
      }

      cls = cls.getSuperclass();
    }
  }

  /**
   * This method calls a method on the specified object. <b>This method takes into account, that the specified object
   * can also be a proxy instance.</b> In this case, the method to be called must be redefined by searching it on the
   * proxy. (Proxy instances are not classes of the type the method was declared in.)
   *
   * @param method
   *        The method to be invoked
   * @param targetObject
   *        The target object or proxy instance.
   * @param args
   *        (Optional) Arguments to pass to the invoked method or <code>null</code> indicating no parameters.
   * @return Returns the return value of the method on demand.
   * @throws IllegalAccessException
   *         Thrown on any access error.
   * @throws SecurityException
   *         Thrown if the reflective operation is not allowed
   * @throws NoSuchMethodException
   *         Thrown if the proxy instance does not provide the desired method.
   * @throws Exception
   *         Thrown if the proxy threw an exception.
   */
  public static Object invokeMethodProxySafe(Method method, Object targetObject, Object... args)
      throws IllegalAccessException, SecurityException, NoSuchMethodException, Exception {
    Method effectiveMethod = method;
    Class<?> clazz = targetObject.getClass();
    if (Proxy.isProxyClass(clazz)) {
      // schuettec - 08.02.2017 : Find the method on the specified proxy.
      effectiveMethod = targetObject.getClass()
          .getMethod(method.getName(), method.getParameterTypes());
    }
    try {
      if (args == null) {
        return effectiveMethod.invoke(targetObject);
      } else {
        return effectiveMethod.invoke(targetObject, args);
      }
    } catch (InvocationTargetException e) {
      handleInvocationTargetException(e);
      return null; // Will never happen because handleInvocationTargetException will always throw.
    }

  }

  /**
   * Unwraps the {@link InvocationTargetException} and throws the resulting exception either as {@link Exception} or
   * {@link Error} to avoid putting reflection-related exception into the exception's cause chain.
   *
   * @param exception
   *        The {@link InvocationTargetException} caught.
   * @throws Exception
   *         The resulting exception to throw.
   */
  public static void handleInvocationTargetException(InvocationTargetException exception) throws Exception {
    Lang.denyNull("exception", exception);
    if (exception.getCause() == null) {
    } else {
      Throwable cause = exception.getCause();
      if (cause instanceof Exception) {
        throw (Exception) cause;
      } else if (cause instanceof Error) {
        throw (Error) cause;
      } else {
        throw new Error("Invocation of proxy threw something else than an exception or error.", cause);
      }
    }
  }
}
