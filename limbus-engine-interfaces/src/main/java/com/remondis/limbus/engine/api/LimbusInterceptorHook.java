package com.remondis.limbus.engine.api;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Interface to implement and inherit {@link LimbusPlugininterceptor} management.
 */
public interface LimbusInterceptorHook extends LimbusPluginInterceptor {

  /**
   * @return Returns the current plugin interceptor.
   */
  public List<LimbusPluginInterceptor> getInterceptors();

  /**
   * Adds the specified {@link LimbusPluginInterceptor}.
   * 
   * @param interceptor The interceptor.
   */
  public default void addLimbusPluginInterceptor(LimbusPluginInterceptor interceptor) {
    requireNonNull(interceptor, "interceptor must not be null!");
    getInterceptors().add(interceptor);
  }

  /**
   * Removes the specified {@link LimbusPluginInterceptor}.
   * 
   * @param interceptor The interceptor.
   */
  public default void removeLimbusPluginInterceptor(LimbusPluginInterceptor interceptor) {
    requireNonNull(interceptor, "interceptor must not be null!");
    getInterceptors().remove(interceptor);
  }

  @Override
  default void beforeContextInvocation(Object plugin, Object proxy, Method method, Object[] args) {
    getInterceptors().stream()
        .forEach(i -> i.beforeContextInvocation(plugin, proxy, method, args));
  }

  @Override
  default void withinContextInvocation(Object plugin, Object proxy, Method method, Object[] args) {
    getInterceptors().stream()
        .forEach(i -> i.withinContextInvocation(plugin, proxy, method, args));
  }

  @Override
  default void withinContextError(Object plugin, Object proxy, Method method, Object[] args, Throwable t) {
    getInterceptors().stream()
        .forEach(i -> i.withinContextError(plugin, proxy, method, args, t));
  }

}
