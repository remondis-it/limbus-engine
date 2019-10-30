package com.remondis.limbus.engine;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.api.LimbusPlugin;
import com.remondis.limbus.engine.api.LimbusContextAction;
import com.remondis.limbus.engine.api.LimbusLifecycleHook;
import com.remondis.limbus.utils.Lang;

/**
 * This is the implementation of the lifecycle proxy handler that enables the lifecycle methods of a
 * {@link LimbusPlugin} to be intercepted. This proxy handler is used to provide the features of a pre-initialize and a
 * post-finish hook.
 *
 * @param
 * <P>
 *        The type of the plugin interface.
 *
 * @author schuettec
 *
 */
public class LifecycleProxyHandler<P extends LimbusPlugin> implements InvocationHandler {
  // schuettec - 27.01.2017 : Do not hold a strong reference to the plugin object. Due to the fact this class mimics the
  // characteristics of IInitializable, it must met the constraint that it is reusable. Hence we cannot null out the
  // reference on finish(). This class must ensure that the plugin object is garbage-collected if there is no strong
  // reference anymore.
  protected WeakReference<P> pluginRef;

  // schuettec - 30.01.2017 : The LimbusContextPublic does not hold a strong reference to the Limbus context.
  protected LimbusContextPublic context;

  // schuettec - 27.01.2017 : The reference to the lifecycle hook may be strong, since it does not hold references to
  // objects from the plugin's classpath or context.
  protected LimbusLifecycleHook<P> lifecycleHook;

  /**
   * Constructs a proxy for the specified plugin object that intercepts the plugin's lifecycle methods.
   *
   * @param context
   *        The <b>public</b> Limbus context is required to not hold a strong reference to the Limbus context. Used to
   *        perform plugin operations in a {@link LimbusContextAction}.
   * @param pluginObject
   *        The object to intercept, <b>may not be null</b>.
   * @param lifecycleHook
   *        (Optional) The lifecycle hook may be <code>null</code>. This parameter is <code>null</code> if the plugin
   *        was requested to initialize without any lifecycle hook.
   */
  public LifecycleProxyHandler(LimbusContextPublic context, P pluginObject, LimbusLifecycleHook<P> lifecycleHook) {
    Lang.denyNull("plugin object", pluginObject);
    this.context = context;
    this.pluginRef = new WeakReference<>(pluginObject);
    this.lifecycleHook = lifecycleHook;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    P plugin = getPluginObjectOrFail();

    // schuettec - 30.01.2017 : Perform all calls on the plugin object within a context action!
    return context.doContextAction(new LimbusContextAction<Object, Throwable>() {
      @Override
      public Object doAction() throws Throwable {
        if (isInitializeMethod(method)) {
          preInitializeOnDemand(plugin);
        } else if (isFinishMethod(method)) {
          postFinishOnDemand(plugin);
        }

        try {
          Method pluginMethod = getPluginMethod(method, plugin);
          if (args == null) {
            return pluginMethod.invoke(plugin);
          } else {
            return pluginMethod.invoke(plugin, args);
          }
        } catch (InvocationTargetException e) {
          // schuettec - 31.01.2017 : Skip InvocationTargetException and throw the cause only if it exists
          Throwable cause = e.getCause();
          if (cause == null) {
            throw e;
          } else {
            throw cause;
          }
        } catch (Exception e) {
          throw e;
        }
      }

      private Method getPluginMethod(Method method, P plugin) {
        // schuettec - 31.01.2017 : We have to find the corresponding method ourselves because abstract classes in the
        // inhertiance hierarchy cannot be called.
        Class<? extends LimbusPlugin> pluginClass = plugin.getClass();
        try {
          Method pluginMethod = pluginClass.getMethod(method.getName(), method.getParameterTypes());
          return pluginMethod;
        } catch (NoSuchMethodException e) {
          // schuettec - 31.01.2017 : CONVENTION: A plugin must implement all methods from its plugin interface
          // (technically this is ensured by the compiler).
          throw new LimbusConventionError(String.format(
              "The plugin implementation %s does not implement the method %s from it's plugin interface %s.",
              pluginClass.getName(), method.toGenericString(), method.getDeclaringClass()
                  .getName()));
        } catch (SecurityException e) {
          throw e;
        }
      }
    });

  }

  /**
   * Calls the lifecycle hook if available. It may be <code>null</code> because it is an optional feature.
   *
   * @param plugin
   *        The current plugin instance
   * @throws Exception
   *         Thrown by the lifecycle hook.
   */
  private void preInitializeOnDemand(P plugin) throws Exception {
    if (hasLifecycleHook()) {
      lifecycleHook.preInitialize(plugin);
    }
  }

  /**
   * Calls the lifecycle hook if available. It may be <code>null</code> because it is an optional feature.
   *
   * @param plugin
   *        The current plugin instance
   * @throws Exception
   *         Thrown by the lifecycle hook.
   */
  private void postFinishOnDemand(P plugin) throws Exception {
    if (hasLifecycleHook()) {
      lifecycleHook.postFinish(plugin);
    }
  }

  private boolean hasLifecycleHook() {
    return lifecycleHook != null;
  }

  private boolean isInitializeMethod(Method method) {
    // schuettec - 30.01.2017 : CONVENTION The constraint here is that IInitializable defines the initialize() method.
    return isMethodOfIInitializable("initialize", method);
  }

  private boolean isFinishMethod(Method method) {
    // schuettec - 30.01.2017 : CONVENTION The constraint here is that IInitializable defines the finish() method.
    return isMethodOfIInitializable("finish", method);
  }

  private boolean isMethodOfIInitializable(String methodName, Method method) throws LimbusConventionError {
    try {
      Method finishOrig = IInitializable.class.getMethod(methodName);
      // TODO - schuettec - 27.01.2017 : Check if the following method compare works. It is okay to assume that the
      // plugin interface and the IInitializable were loaded by the same classloader, since those classes have to be
      // present in the engine's classloader per convention.
      return finishOrig.equals(method);
    } catch (NoSuchMethodException e) {
      // schuettec - 30.01.2017 : CONVENTION The constraint here is that a LimbusPlugin will extend IInitializable. The
      // LimbusPlugin instance is therefore expected to implement the specified method.
      throw new LimbusConventionError(
          "The type com.remondis.limbus.IInitializable was expected to declare the method 'finish()'.");
    } catch (SecurityException e) {
      throw e;
    }
  }

  private P getPluginObjectOrFail() {
    P p = pluginRef.get();
    if (p == null) {
      throw new PluginUndeployedException("The requested plugin was undeployed.");
    }
    return p;
  }

}
