package com.remondis.limbus.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.engine.api.LimbusContextAction;
import com.remondis.limbus.utils.Lang;

/**
 * This class is a collection of workarounds that prevent classloader leaks. There are known issues out there related to
 * JDK classes or third-party libraries that use to hold strong references to objects that where loaded by a particular
 * classloader. Sometimes the classloader itself is held so that garbage collection is not possible.
 *
 * <p>
 * This class provides methods to be called before loading classes from dedicated classloaders. This method calls known
 * workarounds that prevent a bug or similar circumstances to occur. Call {@link #executePreventiveWorkarounds()} to
 * execute this preventative steps.
 * </p>
 * <p>
 * After finishing classes that were loaded by a classloader there are some known issues related to classloader leaks.
 * There are alot of libraries that need to clear their caches or dispose some resources still holding references to
 * classes loaded by a classloader. Call {@link #executeAfterCareWorkarounds()} to execute known workarounds that may
 * drop references to a classloader to be unloaded.
 * </p>
 * <p>
 * It may be possible that some workarounds must be called seperately or at a different time. All workarounds can be
 * called directly.
 * </p>
 *
 * 
 *
 */
public class Workarounds {

  private static final Logger log = LoggerFactory.getLogger(Workarounds.class);

  /**
   * List of ThreadGroup names to ignore when scanning for web application
   * started threads that need to be shut down.
   */
  private static final List<String> JVM_THREAD_GROUP_NAMES = new ArrayList<String>();

  static {
    JVM_THREAD_GROUP_NAMES.add("system");
    JVM_THREAD_GROUP_NAMES.add("RMI Runtime");
  }

  private Workarounds() {
  }

  /**
   * Performs workaround operations that prevent the occurrence of an issue. Call this method as soon as possible to
   * solve some of the known issues.
   */
  public static void executePreventiveWorkarounds() {
    preventAppContextToHoldAReferenceToAClassloaderDifferentFromTheAppClassloader();
    preventAWTThreadsToHoldAReferenceToAClassloaderDifferentFromTheAppClassloader();
  }

  private static void preventAWTThreadsToHoldAReferenceToAClassloaderDifferentFromTheAppClassloader() {
    // TODO - schuettec - 14.12.2016 : Not needed in JRE 9
    // java.awt.Toolkit.getDefaultToolkit();
  }

  /**
   * Performs workaround operations that after care the occurrence of an issue. Call this method to
   * solve some of the known issues.
   */
  public static void executeAfterCareWorkarounds() {

  }

  /**
   * Under some circumstances the <tt>sun.awt.AppContext</tt> may initialize. This happens if code uses graphic
   * operations. When the static initializer in <tt>sun.awt.AppContext</tt> is called it caches the current thread's
   * context classloader. If at this point the current context classloader is a plugin classloader, this classloader may
   * not be unloaded and garbage collected.
   *
   * <p>
   * <b>This method sets the thread's context classloader to the root classloader and performs a call to
   * <tt>sun.awt.AppContext</tt> to trigger its initialization. This should ensure that the reference is held to the
   * root classloader which is safe to hold.</b>
   * </p>
   *
   */
  public static void preventAppContextToHoldAReferenceToAClassloaderDifferentFromTheAppClassloader() {
    // get root classloader
    ClassLoader appClassloader = ClassLoader.getSystemClassLoader();
    ClassLoader oldContext = Thread.currentThread()
        .getContextClassLoader();
    try {
      Thread.currentThread()
          .setContextClassLoader(appClassloader);
      Class.forName("sun.awt.AppContext", true, appClassloader);
    } catch (SecurityException e) {
      throw e;
    } catch (Throwable e) {
    } finally {
      Thread.currentThread()
          .setContextClassLoader(oldContext);
    }
  }

  /*
   * ####################################################################################################
   * Workarounds on undeployment of a plugin classloader.
   * ####################################################################################################
   */

  static void clearResourceBundleCache(PluginClassLoader classloader) {
    try {
      ResourceBundle.clearCache(classloader);
    } catch (SecurityException e) {
      throw e;
    } catch (Throwable t) {
      // keep this silent.
    }
  }

  /**
   * Clears references in known classes that used to hold dangerous references to the classloader directly or
   * indirectly.
   *
   * @param classloader
   *        The plugin classloader to use
   */
  protected static void clearReferencesInKnownClasses(PluginClassLoader classloader) {
    try {
      _callStaticMethod(classloader, "org.apache.commons.logging.LogFactory", "release", types(ClassLoader.class),
          classloader);
      _callStaticMethod(classloader, "java.beans.Introspector", "flushCaches", null);
    } catch (SecurityException e) {
      throw e;
    } catch (Throwable t) {
      // Keep this silent.
    }
  }

  private static Class<?>[] types(Class<?>... params) {
    return params;

  }

  private static void _callStaticMethod(PluginClassLoader classloader, String classname, String methodName,
      Class<?>[] types, Object... params) {
    // Clear references in known classes
    try {
      Class<?> clazz = classloader.loadClass(classname, true);
      if (types != null) {
        Method method = clazz.getMethod(methodName, types);
        method.invoke(null, params);
      } else {
        Method method = clazz.getMethod(methodName);
        method.invoke(null);
      }
    } catch (SecurityException e) {
      throw e;
    } catch (Throwable e) {
      // Keep this silent
    }
  }

  static void nullOutStaticFieldsOfLoadedClasses(PluginClassLoader classloader, List<Class<?>> classes)
      throws Exception {
    doContextAction(classloader, new LimbusContextAction<Void, Exception>() {
      @Override
      public Void doAction() throws Exception {
        Iterator<Class<?>> loadedClasses = classes.iterator();

        // walk through all loaded class to trigger initialization for
        // any uninitialized classes, otherwise initialization of
        // one class may call a previously cleared class.
        while (loadedClasses.hasNext()) {
          Class<?> clazz = loadedClasses.next();
          try {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
              if (Modifier.isStatic(fields[i].getModifiers())) {
                fields[i].get(null);
                break;
              }
            }
          } catch (SecurityException e) {
            throw e;
          } catch (Throwable t) {
            // Ignore
          }
        }

        loadedClasses = classes.iterator();

        while (loadedClasses.hasNext()) {
          Class<?> clazz = loadedClasses.next();
          try {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
              Field field = fields[i];
              int mods = field.getModifiers();
              if (field.getType()
                  .isPrimitive()
                  || (field.getName()
                      .indexOf("$") != -1)) {
                continue;
              }
              if (Modifier.isStatic(mods)) {
                try {
                  field.setAccessible(true);
                  // We cannot clear final field, so we have to follow the reference and clear the resulting object's
                  // fields.
                  if (Modifier.isFinal(mods)) {
                    // But do not modify java. or javax. internal fields (if we would do, we would also null out
                    // internal
                    // fields like classloader?)
                    // if (!((field.getType().getName().startsWith("java."))
                    // || (field.getType().getName().startsWith("javax.")))) {
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                    field.set(null, null);

                    if (log.isTraceEnabled()) {
                      log.trace("Set field " + field.getName() + " to null in class " + clazz.getName());
                    }

                    // nullInstance(field.get(null));
                    // }
                  } else {
                    field.set(null, null);
                    if (log.isTraceEnabled()) {
                      log.trace("Set field " + field.getName() + " to null in class " + clazz.getName());
                    }
                  }
                } catch (SecurityException e) {
                  throw e;
                } catch (Throwable t) {
                  if (log.isTraceEnabled()) {
                    log.trace("Could not set field " + field.getName() + " to null in class " + clazz.getName(), t);
                  }
                }
              }
            }
          } catch (SecurityException e) {
            throw e;
          } catch (Throwable t) {
            if (log.isTraceEnabled()) {
              log.trace("Could not clean fields for class " + clazz.getName(), t);
            }
          }
        }
        return null;
      }
    });
  }

  @SuppressWarnings("rawtypes")
  private static <R, E extends Exception> R doContextAction(PluginClassLoader classloader,
      LimbusContextAction<R, E> callable) throws E {
    Lang.denyNull("Context action", callable);

    // Safe the current context classloader
    ClassLoader contextClassLoaderBefore = Thread.currentThread()
        .getContextClassLoader();

    // Take the thread local before snaphsot.
    Set<ThreadLocal> beforeActionSnapshot = LimbusUtil.getCurrentThreadLocals();

    try {
      // Set the plugin context
      Thread.currentThread()
          .setContextClassLoader(classloader);
      // Perform the actual action
      R retVal = callable.doAction();
      // Return the actual compuation.
      return retVal;
    } finally {
      // Restore the old context classloader
      Thread.currentThread()
          .setContextClassLoader(contextClassLoaderBefore);

      // Take the thread local after snaphsot.
      Set<ThreadLocal> afterActionSnapshot = LimbusUtil.getCurrentThreadLocals();

      // Remove all thread locals added by this context action.
      LimbusUtil.removeAddedThreadLocales(beforeActionSnapshot, afterActionSnapshot);
      LimbusUtil.addThreadLocals(beforeActionSnapshot);
    }
  }

  @SuppressWarnings("deprecation") // thread.stop()
  static void clearReferencingThreads(PluginClassLoader classloader) throws Exception {

    doContextAction(classloader, new LimbusContextAction<Void, Exception>() {
      @Override
      public Void doAction() throws Exception {

        Thread[] threads = getThreads();

        // Iterate over the set of threads
        for (Thread thread : threads) {
          if (thread != null) {
            ClassLoader ccl = thread.getContextClassLoader();
            if (ccl != null && ccl == classloader) {
              // Don't warn about this thread
              if (thread == Thread.currentThread()) {
                continue;
              }

              // Skip threads that have already died
              if (!thread.isAlive()) {
                continue;
              }

              // Don't warn about JVM controlled threads
              ThreadGroup tg = thread.getThreadGroup();
              if (tg != null && JVM_THREAD_GROUP_NAMES.contains(tg.getName())) {
                continue;
              }

              // TimerThread can be stopped safely so treat separately
              if (thread.getClass()
                  .getName()
                  .equals("java.util.TimerThread")) {
                clearReferencesStopTimerThread(thread);
                continue;
              }

              // If the thread has been started via an executor, try
              // shutting down the executor
              try {
                Field targetField = thread.getClass()
                    .getDeclaredField("target");
                targetField.setAccessible(true);
                Object target = targetField.get(thread);

                if (target != null && target.getClass()
                    .getCanonicalName()
                    .equals("java.util.concurrent.ThreadPoolExecutor.Worker")) {
                  Field executorField = target.getClass()
                      .getDeclaredField("this$0");
                  executorField.setAccessible(true);
                  Object executor = executorField.get(target);
                  if (executor instanceof ThreadPoolExecutor) {
                    ((ThreadPoolExecutor) executor).shutdownNow();
                  }
                }
              } catch (SecurityException e) {
                throw e;
              } catch (Throwable t) {
                // Keep this silent.
              }

              // This method is deprecated and for good reason. This is
              // very risky code but is the only option at this point.
              // A *very* good reason for apps to do this clean-up
              // themselves.
              thread.stop();
            }
          }
        }
        return null;
      }
    });
  }

  private static void clearReferencesStopTimerThread(Thread thread) {

    // Need to get references to:
    // - newTasksMayBeScheduled field
    // - queue field
    // - queue.clear()

    try {
      Field newTasksMayBeScheduledField = thread.getClass()
          .getDeclaredField("newTasksMayBeScheduled");
      newTasksMayBeScheduledField.setAccessible(true);
      Field queueField = thread.getClass()
          .getDeclaredField("queue");
      queueField.setAccessible(true);

      Object queue = queueField.get(thread);

      Method clearMethod = queue.getClass()
          .getDeclaredMethod("clear");
      clearMethod.setAccessible(true);

      synchronized (queue) {
        newTasksMayBeScheduledField.setBoolean(thread, false);
        clearMethod.invoke(queue);
        queue.notify(); // In case queue was already empty.
      }

    } catch (SecurityException e) {
      throw e;
    } catch (Throwable t) {
      // Keep this silent!
    }
  }

  /*
   * Get the set of current threads as an array.
   */
  private static Thread[] getThreads() {
    // Get the current thread group
    ThreadGroup tg = Thread.currentThread()
        .getThreadGroup();
    // Find the root thread group
    while (tg.getParent() != null) {
      tg = tg.getParent();
    }

    int threadCountGuess = tg.activeCount() + 50;
    Thread[] threads = new Thread[threadCountGuess];
    int threadCountActual = tg.enumerate(threads);
    // Make sure we don't miss any threads
    while (threadCountActual == threadCountGuess) {
      threadCountGuess *= 2;
      threads = new Thread[threadCountGuess];
      // Note tg.enumerate(Thread[]) silently ignores any threads that
      // can't fit into the array
      threadCountActual = tg.enumerate(threads);
    }

    return threads;
  }

  static void clearJarFileCache(PluginClassLoader classloader, URL[] classpath) {
    try {

      Class<?> jarFileFactory = classloader.loadClass("sun.net.www.protocol.jar.JarFileFactory");
      Method instance = jarFileFactory.getMethod("getInstance");
      instance.setAccessible(true);
      Object retVal = instance.invoke(null);

      for (URL url : classpath) {
        Method get = jarFileFactory.getMethod("get", URL.class);
        get.setAccessible(true);
        Object jarFile = get.invoke(retVal, url);
        Method close = jarFileFactory.getMethod("close", JarFile.class);
        close.setAccessible(true);
        close.invoke(retVal, jarFile);
      }
    } catch (Throwable t) {
      // Keep this silent!
      t.printStackTrace();
    }
  }
}
