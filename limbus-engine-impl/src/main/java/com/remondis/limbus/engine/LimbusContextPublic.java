package com.remondis.limbus.engine;

import java.lang.ref.WeakReference;

import com.remondis.limbus.engine.api.LimbusContext;
import com.remondis.limbus.engine.api.LimbusContextAction;

/**
 * This is the public implementation of {@link LimbusContext}. It can be used to provide access to the Limbus context
 * for clients outside the scope of the Limbus Engine extension. The public {@link LimbusContext} will hold a weak
 * reference to the original Limbus context, so that no memory- or classloader-leaks may occur.
 *
 * @author schuettec
 *
 */
public final class LimbusContextPublic implements LimbusContext {

  private WeakReference<LimbusContextInternal> contextRef;

  LimbusContextPublic(LimbusContextInternal internal) {
    this.contextRef = new WeakReference<>(internal);
  }

  @Override
  public <R, E extends Throwable> R doContextAction(LimbusContextAction<R, E> callable) throws E {
    return getContextOrFail().doContextAction(callable);
  }

  private LimbusContext getContextOrFail() {
    LimbusContextInternal c = contextRef.get();
    if (c == null) {
      throw new PluginUndeployedException("The requested plugin was undeployed.");
    }
    return c;
  }

}
