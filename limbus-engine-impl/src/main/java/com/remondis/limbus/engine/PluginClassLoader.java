package com.remondis.limbus.engine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.properties.LimbusProperties;
import com.remondis.limbus.utils.Lang;

/**
 * The plugin class loader loads resources and classes with child-first strategy. If a request cannot be served by this
 * class loader it is forwared to the parent classloader. This class loader does not delegate directly to any JVM system
 * class loader. Therefore it is not possible to create a plugin class loader without a parent class loader.
 *
 * 
 *
 */
public class PluginClassLoader extends URLClassLoader {

  private static final Logger log = LoggerFactory.getLogger(PluginClassLoader.class);

  private LimbusProperties properties;

  private LimbusFileService filesystem;

  /**
   * If <code>true</code> the classloader will do some cleaning on close().
   */
  protected boolean performCleaning = false;

  /**
   * Holds the shared class loader that enables us to delegate find class.
   */
  protected ClassLoader parent;

  /**
   * Tracking requested InputStreams.
   */
  protected ConcurrentLinkedQueue<WeakReference<InputStreamWrapper>> inputStreamList = new ConcurrentLinkedQueue<WeakReference<InputStreamWrapper>>();

  /**
   * The set of url to load.
   */
  private URL[] urls;

  /**
   * Holds the set of permissions granted for classes in the classpath of this classloader.
   */
  private Set<Permission> permissions = new HashSet<Permission>();

  /**
   * Holds the set of permissions that are required to allow every class of the classpath to access a resource of the
   * classpath.
   */
  private Set<Permission> codeSourcePermissions = new HashSet<Permission>();

  /**
   * Creates a {@link PluginClassLoader} with the specified URLs to add to its repository. Requests that cannot be
   * serverd by this class loader are delegated to the specified parent.
   *
   * @param filesystem
   *        The filesystem abstraction to read the {@link LimbusProperties} from.
   * @param urls
   *        The list of urls making up the repository of this class loader.
   * @param parent
   *        The parent class loader to delegate to.
   * @param factory
   *        The {@link URLStreamHandlerFactory} used to create URLs
   */
  public PluginClassLoader(LimbusFileService filesystem, ClassLoader parent, URLStreamHandlerFactory factory,
      URL... urls) {
    super(urls, Lang.denyNull("parent", parent), factory);
    Lang.denyNull("filesystem", filesystem);
    Lang.denyNull("urls", urls);
    this.filesystem = filesystem;
    this.urls = urls;
    this.parent = parent;
    initialize();
  }

  public PluginClassLoader(LimbusFileService filesystem, ClassLoader parent, URL... urls) {
    super(urls, Lang.denyNull("parent", parent));
    Lang.denyNull("filesystem", filesystem);
    Lang.denyNull("urls", urls);
    this.filesystem = filesystem;
    this.urls = urls;
    this.parent = parent;
    initialize();
  }

  public PluginClassLoader(LimbusFileService filesystem, ClassLoader parent, Set<URL> urls) {
    this(filesystem, Lang.denyNull("parent", parent), urls.toArray(new URL[urls.size()]));
  }

  private void initialize() throws RuntimeException {
    try {
      this.properties = new LimbusProperties(filesystem, PluginClassLoader.class, true, false);
    } catch (Exception e) {
      throw new RuntimeException("Cannot get properties for plugin classloader.", e);
    }

    // Collect the set of permissions required to access every code source within this classpath
    for (URL url : urls) {
      try {
        Permission permission = url.openConnection()
            .getPermission();
        if (permission != null) {
          codeSourcePermissions.add(permission);
        }
      } catch (IOException e) {
        throw new RuntimeException(
            String.format("Cannot request required permissions to connect to the URL %s", url.toString()), e);
      }
    }
  }

  private boolean trackStreams() {
    return properties.getBoolean("trackStreams");
  }

  private boolean closeTrackedStreams() {
    return properties.getBoolean("closeTrackedStreams");
  }

  private boolean stopStartedThreads() {
    return properties.getBoolean("stopStartedThreads");
  }

  private boolean nullOutStaticFields() {
    return properties.getBoolean("nullOutStaticFields");
  }

  /**
   * Sets the permissions granted for classes in this classpath.
   * <p>
   * <b>Note: The classloader grants file permissions for all URLs in this classpath. The permissions specified by this
   * method are added.</b>
   * </p>
   *
   * @param permissions
   *        The permissions to grant additionally.
   */
  public void setPermissions(Set<Permission> permissions) {
    this.permissions = new HashSet<Permission>();
    this.permissions.addAll(permissions);
  }

  public void deactivateCleaning() {
    performCleaning = false;
  }

  @Override
  protected PermissionCollection getPermissions(CodeSource codeSource) {
    PermissionCollection pc;
    pc = super.getPermissions(codeSource);
    addPermissions(pc);
    return (pc);
  }

  public Set<Permission> getPermissions() {
    HashSet<Permission> permissions = new HashSet<>(this.permissions);

    for (Permission p : permissions) {
      permissions.add(p);
    }
    for (Permission p : codeSourcePermissions) {
      permissions.add(p);
    }
    return permissions;
  }

  /**
   * Adds the permissions that were set on this classloader to the specified collection.
   *
   * @param pc
   *        The permission collection to add the permissions.
   */
  private void addPermissions(PermissionCollection pc) {
    for (Permission p : permissions) {
      pc.add(p);
    }
    for (Permission p : codeSourcePermissions) {
      pc.add(p);
    }
  }

  @Override
  public URL getResource(String name) {
    URL url = null;

    // Child first lookup
    url = findResource(name);
    if (log.isTraceEnabled()) {
      if (url == null) {
        log.trace("Plugin is delegating to parent for recource: {}", name);
      } else {
        log.trace("Plugin loader is loading resource: {}", name);
      }
    }

    if (url == null) {
      // If not found delegate.
      url = parent.getResource(name);
      if (log.isTraceEnabled()) {
        if (url == null) {
          log.trace("Resource not found by shared classloader: {}", name);
        } else {
          log.trace("Resource loaded by shared classloader: {}", name);
        }
      }
    }
    return url;
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    // getResourceAsStream() uses getResource() that is overridden above and therefore uses child first strategy.

    InputStream stream = PluginClassLoader.super.getResourceAsStream(name);
    if (stream == null) {
      return null;
    } else {
      if (trackStreams()) {
        InputStreamWrapper resourceAsStream = new InputStreamWrapper(stream);
        inputStreamList.add(new WeakReference<InputStreamWrapper>(resourceAsStream));
        return resourceAsStream;
      } else {
        return stream;
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if (log.isTraceEnabled()) {
      log.trace("Plugin loader is loading: {}", name);
    }

    @SuppressWarnings("rawtypes")
    Enumeration[] tmp = new Enumeration[2];

    tmp[0] = findResources(name);
    if (tmp[0] == null) {
      if (log.isTraceEnabled()) {
        log.trace("Delegating to shared classloader: {}", name);
      }
      tmp[0] = parent.getResources(name);
    } else {
      if (log.isTraceEnabled()) {
        log.trace("Adding also results from shared classloader: {}", name);
      }
      tmp[1] = parent.getResources(name);
    }

    return new CompoundEnumeration<>(tmp);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (log.isTraceEnabled()) {
      log.trace("Plugin loader is loading: {}", name);
    }

    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c == null) {
        // If not loaded, then invoke findClass to search for that class in this repository first.
        try {
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          // ClassNotFoundException thrown if class not found by this classloader
          // Then delegate to parent
          if (log.isTraceEnabled()) {
            log.trace("Delegating to shared classloader: {}", name);
          }
          c = parentLoadClass(parent, name, resolve);
        }
      }

      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }

  @Override
  public void close() {

    AccessController.doPrivileged(new PrivilegedAction<Void>() {

      @Override
      public Void run() {
        System.setProperty("java.security.debug", "");

        try {
          if (performCleaning) {
            performUndeployWorkarounds(PluginClassLoader.this, getLoadedClasses());
          }
        } catch (SecurityException e) {
          log.error("One of the memory/classloader leak detections threw a security exception.", e);
        } catch (Throwable t) {
          // Avoid exposing plugin classes via stacktrace
          // Keep this silent!
        } finally {
          /*
           * The following methods are commented out because they are not neccessary for clean undeploying at the
           * moment.
           */
          // Forget all classes loaded
          // clearClasses();

          // Close all URL stream connections
          try {
            PluginClassLoader.super.close();
          } catch (Exception e) {
            // Close silently
            log.warn("Could not close classloader of classpath. Exception was: ", e);
          }

          // Clear URL cache in JarFileFactory
          // Workarounds.clearJarFileCache(PluginClassLoader.this, urls);

          // Trigger GC
          System.gc();

          // Warn for open streams
          warnForOpenStreamsOnDemand();

          // Close tracked streams - This feature is controlled by properties
          closeAllRequestedStreamsOnDemand();

          permissions = null;

          // Forget the parent classloader
          parent = null;
        }

        return null;
      }
    });

  }

  private void warnForOpenStreamsOnDemand() {
    if (trackStreams()) {
      Iterator<WeakReference<InputStreamWrapper>> it = inputStreamList.iterator();
      int unclosed = 0;
      while (it.hasNext()) {
        WeakReference<InputStreamWrapper> ref = it.next();
        // If reference not cleared count!
        if (ref.get() != null) {
          unclosed++;
        }
      }
      if (unclosed > 0) {
        log.warn("There were {} unclosed input streams while undeploying a plugin context.", unclosed);
      }
    }
  }

  private void closeAllRequestedStreamsOnDemand() {
    if (closeTrackedStreams()) {
      Iterator<WeakReference<InputStreamWrapper>> iterator = inputStreamList.iterator();
      while (iterator.hasNext()) {
        WeakReference<InputStreamWrapper> reference = iterator.next();
        InputStreamWrapper stream = reference.get();
        Lang.closeQuietly(stream);
        stream = null;
        System.gc();
      }
    }
  }

  @SuppressWarnings("unused")
  private void clearClasses() {
    try {
      // Reflectively get the loaded classes Vector
      Field field = ClassLoader.class.getDeclaredField("classes");
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      field.setAccessible(true);
      field.set(null, null);
    } catch (Throwable t) {
      log.error("Cannot inspect all loaded classes.", t);
    }
  }

  @SuppressWarnings("unused")
  private void nullInstance(Object instance) {
    if (instance == null) {
      return;
    }
    Field[] fields = instance.getClass()
        .getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      int mods = field.getModifiers();
      if (field.getType()
          .isPrimitive()
          || (field.getName()
              .indexOf("$") != -1)) {
        continue;
      }
      try {
        field.setAccessible(true);
        if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
          // Doing something recursively is too risky
          continue;
        }
        Object value = field.get(instance);
        if (null != value) {
          Class<? extends Object> valueClass = value.getClass();
          if (!loadedByThisOrChild(valueClass)) {
            if (log.isTraceEnabled()) {
              log.trace("Not setting field " + field.getName() + " to null in object of class " + instance.getClass()
                  .getName() + " because the referenced object was of type " + valueClass.getName()
                  + " which was not loaded by this WebappClassLoader.");
            }
          } else {
            field.set(instance, null);
            if (log.isTraceEnabled()) {
              log.trace("Set field " + field.getName() + " to null in class " + instance.getClass()
                  .getName());
            }
          }
        }
      } catch (Throwable t) {
        if (log.isTraceEnabled()) {
          log.trace(
              "Could not set field " + field.getName() + " to null in object instance of class " + instance.getClass()
                  .getName(),
              t);
        }
      }
    }
  }

  /**
   * Determine whether a class was loaded by this class loader or one of
   * its child class loaders.
   *
   * @param clazz
   * @return
   */
  protected boolean loadedByThisOrChild(Class<? extends Object> clazz) {
    boolean result = false;
    for (ClassLoader classLoader = clazz.getClassLoader(); null != classLoader; classLoader = classLoader.getParent()) {
      if (classLoader.equals(this)) {
        result = true;
        break;
      }
    }
    return result;
  }

  @SuppressWarnings({
      "rawtypes", "unchecked"
  }) // In Java 8 the classes field in java.lang.ClassLoader was a java.util.Vector
  private List<Class<?>> getLoadedClasses() {
    List<Class<?>> loadedClasses = Collections.emptyList();
    try {
      // Reflectively get the loaded classes Vector
      Field classesField = ClassLoader.class.getDeclaredField("classes");
      classesField.setAccessible(true);
      Object object = classesField.get(this);
      loadedClasses = new ArrayList<Class<?>>((Vector) object);
    } catch (Throwable t) {
      log.error("Cannot inspect all loaded classes.", t);
    }
    return loadedClasses;
  }

  protected Class<?> parentLoadClass(ClassLoader parent, String name, boolean b) throws ClassNotFoundException {
    try {
      // schuettec - 07.03.2017 : Currently a reflective hack solution is used here to make sure the resolve-flag is
      // passed to the parent classloader's loadClass-method. Another solution is to analyze what could go wrong if
      // parent.loadClass() is used (since it sets always false for the resolve-flag). The resolving could then happen
      // on demand.
      return parent.loadClass(name);
    } catch (Throwable e) {
      throw new ClassNotFoundException(String.format("Class %s cannot be found.", name), e);
    }

  }

  /**
   * This method performs workarounds to avoid classloader leaks. This method should only be called by the
   * {@link PluginClassLoader}. The {@link PluginClassLoader} makes sure this operations will run in a privileged action
   * with all access granted on Limbus Engine level.
   *
   * @param classloader
   *        The plugin classloader
   * @param loadedClasses
   *        The list of classes loaded by the {@link PluginClassLoader}.
   * @throws Exception
   *         Thrown on any error.
   */
  protected void performUndeployWorkarounds(PluginClassLoader classloader, List<Class<?>> loadedClasses)
      throws Exception {

    Exception exception = null;

    // Clears references in known classes that used to hold dangerous references to the classloader directly or
    // indirectly.
    try {
      Workarounds.clearReferencesInKnownClasses(classloader);
    } catch (Throwable e) {
      exception = createOrAppendException(exception, e);
    }

    // Stops all threads that reference the plugin classloader as context classloader with all alive non-JVM-controlled
    // threads incl. timer threads
    try {
      if (stopStartedThreads()) {
        Workarounds.clearReferencingThreads(classloader);
      }
    } catch (Throwable e) {
      exception = createOrAppendException(exception, e);
    }

    try {
      if (nullOutStaticFields()) {
        Workarounds.nullOutStaticFieldsOfLoadedClasses(classloader, loadedClasses);
      }
    } catch (Throwable e) {
      exception = createOrAppendException(exception, e);
    }

    // Clear caches of ResourceBundle because it holds a cache of loaded resources
    try {
      Workarounds.clearResourceBundleCache(classloader);
    } catch (Throwable e) {
      exception = createOrAppendException(exception, e);
    }

    // Iterates over all Drivers registered by the specified classloader to unregister them.
    // deregisterJDBCDrivers(classloader);

    throwOnDemand(exception);
  }

  private static void throwOnDemand(Exception exception) throws Exception {
    if (exception != null) {
      throw exception;
    }
  }

  private static Exception createOrAppendException(Exception exception, Throwable toAppend) {
    if (exception == null) {
      return new Exception("Cannot perform some of the memory/classloader leak preventions.", toAppend);
    } else {
      exception.addSuppressed(toAppend);
      return exception;
    }
  }

}

/*
 * A utility class that will enumerate over an array of enumerations.
 */
final class CompoundEnumeration<E> implements Enumeration<E> {
  private final Enumeration<E>[] enums;
  private int index;

  public CompoundEnumeration(Enumeration<E>[] enums) {
    this.enums = enums;
  }

  private boolean next() {
    while (index < enums.length) {
      if (enums[index] != null && enums[index].hasMoreElements()) {
        return true;
      }
      index++;
    }
    return false;
  }

  @Override
  public boolean hasMoreElements() {
    return next();
  }

  @Override
  public E nextElement() {
    if (!next()) {
      throw new NoSuchElementException();
    }
    return enums[index].nextElement();
  }
}
