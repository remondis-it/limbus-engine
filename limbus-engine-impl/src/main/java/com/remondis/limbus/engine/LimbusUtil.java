package com.remondis.limbus.engine;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.LimbusClasspathException;
import com.remondis.limbus.api.LimbusPlugin;
import com.remondis.limbus.utils.Lang;

public final class LimbusUtil {

  public static final String PUBLIC_LIMBUS_API_PACKAGE_PREFIX = LimbusPlugin.class.getPackage()
      .getName();

  private LimbusUtil() {
  }

  /**
   * @return Returns the default accessible package prefixes. Used for test purposes.
   */
  public static final List<String> getDefaultAllowedPackagePrefixes() {
    return Collections.unmodifiableList(Arrays.asList(PUBLIC_LIMBUS_API_PACKAGE_PREFIX));
  }

  /**
   * @return Returns <code>true</code> if the current action is executed by Limbus plugin code otherwise
   *         <code>false</code> is returned.
   */
  static boolean isPluginCaller() {
    ClassLoader classLoader = Thread.currentThread()
        .getContextClassLoader();
    do {
      if (classLoader instanceof PluginClassLoader) {
        return true;
      }
    } while (classLoader != null && (classLoader = classLoader.getParent()) != null);
    return false;
  }

  static void logClasspath(String classpathName, Classpath classPath, Logger log) {
    Lang.denyNull("classpath name", classpathName);
    Lang.denyNull("classpath", classPath);
    Lang.denyNull("log", log);
    if (log.isDebugEnabled()) {

      Set<URL> urls = classPath.getClasspath();

      if (urls.isEmpty()) {
        log.info("Logging {} classpath: Empty classpath.", classpathName);
      } else {
        log.info("Logging {} classpath:", classpathName);
        for (URL url : urls) {
          log.debug("URL entry: {}", url.toString());
        }
      }
    }

  }

  @SuppressWarnings("rawtypes")
  static Set<ThreadLocal> getCurrentThreadLocals() {
    Set<ThreadLocal> threadLocalsSet = new HashSet<>();

    try {
      // Get a reference to the thread locals table of the current thread
      Thread thread = Thread.currentThread();
      Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
      threadLocalsField.setAccessible(true);
      Object threadLocalTable = threadLocalsField.get(thread);

      if (threadLocalTable != null) {
        // Get a reference to the array holding the thread local variables inside the
        // ThreadLocalMap of the current thread
        Class threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
        Field tableField = threadLocalMapClass.getDeclaredField("table");
        tableField.setAccessible(true);
        Object table = tableField.get(threadLocalTable);
        if (table != null) {
          for (int i = 0; i < Array.getLength(table); i++) {
            // Each entry in the table array of ThreadLocalMap is an Entry object
            // representing the thread local reference and its value
            Object entry = Array.get(table, i);
            if (entry != null) {
              // ThreadLocal threadLocal = (ThreadLocal) referentField.get(entry);
              ThreadLocal threadLocal = (ThreadLocal) ((WeakReference) entry).get();
              // schuettec - 01.02.2017 : Reference value may be null here.
              if (threadLocal != null) {
                threadLocalsSet.add(threadLocal);
              }
            }
          }
        }
      }

      return threadLocalsSet;

    } catch (Exception e) {
      // TODO - Buschmann - 10.10.2016 : Analyse if this can happen
      throw new IllegalStateException("Cannot lookup thread locals.", e);
    }

  }

  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  static void addThreadLocals(Set<ThreadLocal> threadLocalsSet) {
    Lang.denyNull("ThreadLocals", threadLocalsSet);

    for (ThreadLocal threadLocal : threadLocalsSet) {
      // Stores a new thread local in the current thread
      threadLocal.set(threadLocal.get());
    }
  }

  /**
   * Removes the thread locals from the current thread that were added after taking the before snapshot.
   *
   * @param beforeActionSnapshot
   *        The snapshot before
   * @param afterActionSnapshot
   *        The snapshot after
   * @return Returns those {@link ThreadLocal} objects that were removed from the thread.
   */
  @SuppressWarnings({
      "rawtypes"
  })
  static Set<ThreadLocal> removeAddedThreadLocales(Set<ThreadLocal> beforeActionSnapshot,
      Set<ThreadLocal> afterActionSnapshot) {
    Set<ThreadLocal> threadLocalsDelta = new HashSet<>();

    // Check which ThreadLocals were created by the context action
    for (ThreadLocal threadLocal : afterActionSnapshot) {
      if (!beforeActionSnapshot.contains(threadLocal)) {
        threadLocalsDelta.add(threadLocal);
        // Remove threadLocal from current thread because it was added by the context action
        threadLocal.remove();
      }
    }

    return threadLocalsDelta;
  }

  @SuppressWarnings({
      "rawtypes"
  })
  static void storeThreadLocalsInDeployContext(Set<ThreadLocal> beforeActionSnapshot,
      Set<ThreadLocal> afterActionSnapshot, LimbusContextInternal limbusContext) {

    Set<ThreadLocal> threadLocalsDelta = removeAddedThreadLocales(beforeActionSnapshot, afterActionSnapshot);
    // Store the ThreadLocals added from the context action in limbus context
    limbusContext.setThreadLocalsSet(threadLocalsDelta);

    // Restore the ThreadLocals to the state before a context action was executed
    LimbusUtil.addThreadLocals(beforeActionSnapshot);
  }

  static boolean isLimbusPlugin(ClassLoader limbusClassloader, String className) {
    Lang.denyNull("Classloader", limbusClassloader);
    Lang.denyNull("Classname", className);

    Class<?> clazz;
    try {
      // schuettec - 04.10.2016 : In the past we used Class.forName here (see next code line). This suffers from the
      // effect that classes loaded this way kept in memory for ever.
      // clazz = Class.forName(className, false, limbusClassloader);
      // schuettec - 06.10.2016 : Another thing to notice is, that here is not LimbusContextAction used to load the
      // class. This is because we do not initialize the class. We simply load it to know the type. No plugin code is
      // running here, so not LimbusContextAction is needed.
      clazz = getClass(limbusClassloader, className);
      return LimbusPlugin.class.isAssignableFrom(clazz) && !LimbusPlugin.class.equals(clazz) && !clazz.isInterface()
          && !Modifier.isAbstract(clazz.getModifiers());
    } catch (Throwable e) {
      // Ignore throwables is the only way to go here: We want to analyze classes for their types. This means the
      // classloader has to load them. He often will try to load classes that are not available in the classpath. Often
      // libraries have references to classes that are provided at runtime only if a specific feature is used at
      // runtime. Those optional classes will cause NoClassDefFoundErrors at this point.
      return false;
    }
  }

  static void denyClassNotFound(ClassLoader limbusClassloader, String className) throws LimbusClasspathException {
    try {
      getClass(limbusClassloader, className);
    } catch (ClassNotFoundException e) {
      throw new LimbusClasspathException(String.format("The class %s is not deployed on this classpath.", className),
          e);
    }

  }

  private static Class<?> getClass(ClassLoader limbusClassloader, String className) throws ClassNotFoundException {
    return limbusClassloader.loadClass(className);
  }

  static List<String> getClassNames(URL url) throws LimbusClasspathException {
    Lang.denyNull("URL", url);
    ZipInputStream inputStream = null;
    try {
      List<String> classNames = new ArrayList<String>();
      URLConnection connection = url.openConnection();
      inputStream = new ZipInputStream(connection.getInputStream());
      for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {
        if (!entry.isDirectory() && entry.getName()
            .endsWith(".class")) {
          // This ZipEntry represents a class. Now, what class does it represent?
          String className = entry.getName()
              .replace('/', '.'); // including ".class"
          classNames.add(className.substring(0, className.length() - ".class".length()));
        }
        inputStream.closeEntry();
      }
      return classNames;
    } catch (Exception e) {
      throw new LimbusClasspathException("Cannot scan the contents of " + url.toString(), e);
    } finally {
      Lang.closeQuietly(inputStream);
    }
  }

  public static void logPermissions(String classpathName, Set<Permission> permissions, Logger log) {
    Lang.denyNull("classpath name", classpathName);
    Lang.denyNull("permissions", permissions);
    Lang.denyNull("log", log);
    if (log.isDebugEnabled()) {
      if (permissions.isEmpty()) {
        log.info("Granting no permissions to classpath {}.", classpathName);
      } else {
        log.info("Granting the following permissions to classpath {}.", classpathName);
        for (Permission p : permissions) {
          log.debug("Granting permission {}", p.toString());
        }
      }
    }
  }

  /**
   * This method checks the reachability of the specified object. This method is used to trigger the garbage collection
   * ad-hoc. <b>This method may give different results on different platforms. If this method returns
   * <code>false</code> this does not mean that the object cannot be garbage collected at all.</b> This is because the
   * garbage collection cannot be triggered deterministically. Use the {@link LimbusReferenceObserver} to check for
   * garbage collection safely, but keep in mind that the reference observer may take a long time until notifying about
   * garbage collection (<300s).
   *
   * <p>
   * <b>This method assumes that there are no strong references to the specified object. If there are some, the result
   * of this method is always <code>false</code>.</b>
   * </p>
   *
   * @param reference
   *        The object to be checked for direct garbage collection
   * @return Returns <code>true</code> if an object could be garbage collected using {@link System#gc()}, otherwise
   *         <code>false</code> is returns. Keep in mind that <code>false</code> does not mean that the object generally
   *         cannot be garbage collected.
   */
  static boolean isGarbageCollected(WeakReference<Object> reference) {
    System.gc();
    return (reference.get() == null);
  }

}
