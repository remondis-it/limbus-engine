package com.remondis.limbus.logging;

import java.util.Map;

/**
 * This is an implementation of the {@link TargetSelector} that selects a specified target identified by the current
 * thread's context classloader.
 *
 * @param <T>
 *        The type of the target.
 *
 * @author schuettec
 *
 */
public class ContextClassloaderSelector<T> implements TargetSelector<Integer, T> {

  @Override
  public T selectTarget(Map<Integer, T> targets) {
    int systemHashCode = getCurrentClassLoaderHashCode();
    T target = targets.get(systemHashCode);
    return target;
  }

  /**
   * @return Returns the identity hash code of the current thread's context classloader.
   */
  public static int getCurrentClassLoaderHashCode() {
    ClassLoader classLoader = Thread.currentThread()
        .getContextClassLoader();
    int systemHashCode = System.identityHashCode(classLoader);
    return systemHashCode;
  }

  /**
   * @return Returns the identity hash code of the current thread.
   */
  public static int getCurrentThreadHashCode() {
    return System.identityHashCode(Thread.currentThread());
  }

  /**
   * @param classLoader
   *        The classloader to get the hashcode for.
   * @return Returns the identity hashcode of the specified classloader.
   */
  public static int getClassLoaderHashCode(ClassLoader classLoader) {
    return System.identityHashCode(classLoader);
  }

  /**
   * @param thread
   *        The thread to get the hashcode for.
   * @return Returns the identity hashcode of the specified thread.
   */
  public static int getThreadHashCode(Thread thread) {
    return System.identityHashCode(thread);
  }

}
