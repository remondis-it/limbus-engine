package com.remondis.limbus.launcher;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.system.LimbusSystem;

/**
 * The {@link SystemEngine} is an implementation of {@link AbstractEngine} and integrates the Limbus System into a
 * runnable engine. It is then possible to use the component management of {@link LimbusSystem}.
 *
 * <h2>Configuration</h2>
 * The {@link SystemEngine} needs a system description to initialize the Limbus System component management. The system
 * description is assumed to be present in the classpath with filename {@value #DEFAULT_SYSTEM_FILENAME}. The descriptor
 * can also be passed as {@link InputStream} using the constructor {@link #SystemEngine(InputStream)} or the
 * uninitialized instance of {@link LimbusSystem} can be passed using {@link #SystemEngine(LimbusSystem)}.
 *
 * @author schuettec
 *
 */
public class SystemEngine extends AbstractEngine {

  /**
   * Holds the Limbus System to manage components of this engine.
   */
  protected LimbusSystem system;

  /**
   * Constructs a System Engine that reads the system descriptor using the path configured in the Limbus Properties for
   * this class.
   */
  public SystemEngine() {

  }

  /**
   * Constructs a System Engine that uses the specified uninitialized {@link LimbusSystem}.
   *
   * @param system
   */
  public SystemEngine(LimbusSystem system) {
    this.system = system;
  }

  @Override
  protected void performInitialize() throws Exception {
    this.system.initialize();
  }

  @Override
  protected void performFinish() {
    // Stop the Limbus system
    if (this.system != null) {
      this.system.finish();
      this.system = null;
    }
  }

  /**
   * Provides access to a Limbus component.
   *
   * @param requestType
   *        The request type of the component.
   * @return Returns the Limbus component.
   * @see com.remondis.limbus.system.LimbusSystem#getComponent(java.lang.Class)
   */
  public <T extends IInitializable<?>> T getComponent(Class<T> requestType) {
    return system.getComponent(requestType);
  }

}
