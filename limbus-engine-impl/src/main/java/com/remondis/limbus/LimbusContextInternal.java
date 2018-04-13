package com.remondis.limbus;

import static com.remondis.limbus.LimbusUtil.getCurrentThreadLocals;

import java.util.HashSet;
import java.util.Set;

import com.remondis.limbus.utils.Lang;

/**
 * This is the implementation of the Limbus Context. This implementation is assumed to be the only implementation
 * available. The interfaces are only used to provide an internal an external interface for accessing a Limbus
 * context.
 *
 * @author schuettec
 *
 */
public final class LimbusContextInternal implements LimbusContext {

  private PluginClassLoader classloader;

  @SuppressWarnings("rawtypes")
  private Set<ThreadLocal> threadLocalsSet;

  /**
   * Constructs an empty limbus context.
   */
  public LimbusContextInternal() {
    threadLocalsSet = new HashSet<>();
  }

  LimbusContextInternal(PluginClassLoader classloader) {
    this.classloader = classloader;
    threadLocalsSet = new HashSet<>();
  }

  PluginClassLoader getClassloader() {
    return classloader;
  }

  @SuppressWarnings("rawtypes")
  Set<ThreadLocal> getThreadLocalsSet() {
    return threadLocalsSet;
  }

  void setThreadLocalsSet(@SuppressWarnings("rawtypes") Set<ThreadLocal> threadLocalsSet) {
    this.threadLocalsSet = threadLocalsSet;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public <R, E extends Throwable> R doContextAction(LimbusContextAction<R, E> callable) throws E {
    Lang.denyNull("Context action", callable);

    // Safe the current context classloader
    ClassLoader contextClassLoaderBefore = Thread.currentThread()
        .getContextClassLoader();

    // Take the thread local before snaphsot.
    Set<ThreadLocal> beforeActionSnapshot = getCurrentThreadLocals();

    // Add the plugin's thread locals recorded before
    LimbusUtil.addThreadLocals(getThreadLocalsSet());

    try {
      // Set the plugin context
      Thread.currentThread()
          .setContextClassLoader(getClassloader());
      // Perform the actual action
      R retVal = callable.doAction();
      // Return the actual compuation.
      return retVal;
    } finally {
      // Restore the old context classloader
      Thread.currentThread()
          .setContextClassLoader(contextClassLoaderBefore);

      // Take the thread local after snaphsot.
      Set<ThreadLocal> afterActionSnapshot = getCurrentThreadLocals();
      // Do the combi-action of removing all added thread locals and store the added ones to the plugin's thread local
      // management.
      LimbusUtil.storeThreadLocalsInDeployContext(beforeActionSnapshot, afterActionSnapshot, this);
    }
  }

  void finish() {
    // Clear thread locals
    threadLocalsSet.clear();
    threadLocalsSet = null;

    // Close the open URLs of the classpath
    this.classloader.close();

    // Remove the classloader
    classloader = null;

  }
}
