package com.remondis.limbus.utils;

import static com.remondis.limbus.utils.ReflectionUtil.newInstance;
import static java.util.Objects.isNull;

import java.util.Hashtable;
import java.util.Map;

/**
 * Util class to manage storage and access of {@link ThreadLocal} variable.
 */
public class ThreadContextUtil {

  private static ThreadLocal<ThreadContextHolder> threadLocal = new ThreadLocal<ThreadContextHolder>();

  private static class ThreadContextHolder {
    private Map<String, Object> threadVariables;

    ThreadContextHolder() {
      super();
      this.threadVariables = new Hashtable<>();
    }

    @SuppressWarnings("unchecked")
    <T> T getBean(Class<T> beanType) {
      T value = null;
      String variableKey = getVariableKey(beanType);
      if (hasBean(beanType)) {
        value = (T) threadVariables.get(variableKey);
      } else {
        value = createBean(beanType);
        threadVariables.put(variableKey, value);
      }
      return value;
    }

    public boolean hasBean(Class<?> beanType) {
      String variableKey = getVariableKey(beanType);
      return threadVariables.containsKey(variableKey);
    }

    <T> T createBean(Class<T> beanType) {
      try {
        T newBean = newInstance(beanType);
        return newBean;
      } catch (Exception e) {
        throw new RuntimeException(
            "Could not create thread local variable. The following type does not have an accessible default constructor: "
                + beanType.getName());
      }
    }

    String getVariableKey(Class<?> beanType) {
      return beanType.getName();
    }

  }

  /**
   * Returns the thread context bean of the desired type if present. Otherwise a new bean instance of this type is
   * created and stored in the current thread.
   * 
   * @param <T> The type of the thread context bean.
   * @param threadContextObject The type of the thread context bean.
   * @return Returns the thread context bean of the current thread.
   */
  public static <T> T getThreadContext(Class<T> threadContextBean) {
    ThreadContextHolder threadContextHolder = getOrCreateThreadContextHolder();
    return threadContextHolder.getBean(threadContextBean);
  }

  /**
   * Clears the thread context of the current thread and removes the managing {@link ThreadLocal}.
   */
  public static void clearThreadContext() {
    threadLocal.remove();
  }

  public static boolean hasThreadContext(Class<?> threadContextBean) {
    ThreadContextHolder threadContextHolder = threadLocal.get();
    if (isNull(threadContextHolder)) {
      return false;
    } else {
      return threadContextHolder.hasBean(threadContextBean);
    }
  }

  private static ThreadContextHolder getOrCreateThreadContextHolder() {
    ThreadContextHolder threadContextHolder = threadLocal.get();
    if (isNull(threadContextHolder)) {
      ThreadContextHolder newThreadContextHolder = new ThreadContextHolder();
      threadContextHolder = newThreadContextHolder;
      threadLocal.set(threadContextHolder);
      threadContextHolder = threadLocal.get();
    }
    return threadContextHolder;
  }

}
