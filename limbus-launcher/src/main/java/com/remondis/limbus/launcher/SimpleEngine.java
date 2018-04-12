package com.remondis.limbus.launcher;

import com.remondis.limbus.IInitializable;
import com.remondis.limbus.events.EventMulticaster;
import com.remondis.limbus.events.EventMulticasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements core functions of a service daemon. The daemon should run until it is requested by the runtime
 * environment to terminate.
 * <p>
 * When started this {@link AbstractEngine} creates a local thread that observes the engine's state. If a termination
 * signal was caught by the environment the thread enters the shutdown sequence and asks all known processes to
 * terminate. Finally all engine resources are cleard and the engine will stop.
 * </p>
 *
 * <h2>Shutdown sequence</h2>
 * <p>
 * If the engine receives a shutdown command the engine observer starts the shutdown sequence. All known processes are
 * requested to stop. This may take a while since there can be "long" running tasks to do while shutting down. If there
 * are abandoned threads or processes that do not stop in a specified amount of time (configurable using
 * {@link #SHUTDOWN_TIMEOUT}) the engine is shut down forcibly.
 * </p>
 *
 * @author schuettec
 *
 */
public abstract class SimpleEngine extends AbstractEngine {

  private static final Logger log = LoggerFactory.getLogger(SimpleEngine.class);

  @SuppressWarnings("rawtypes")
  protected EventMulticaster<IInitializable> systemComponents;

  /**
   * Called by the engine to create the system components to be bound to the engine's lifecycle. The lifecycle of the
   * {@link IInitializable}s is managed by the engine.
   *
   * @return Returns the uninitialized system components.
   */
  public abstract IInitializable<?>[] createSystemComponents();

  @Override
  protected void performInitialize() throws Exception {
    this.systemComponents = EventMulticasterFactory.create(IInitializable.class);

    // Register all system components
    registerAllSystemComponents();

    // Initialize sequence: Multicast initialize event
    systemComponents.multicast()
        .initialize();

  }

  private void registerAllSystemComponents() {
    // Get system components:
    IInitializable<?>[] systemComponents = createSystemComponents();
    for (IInitializable<?> component : systemComponents) {
      this.systemComponents.addSubscriber(component);
    }
  }

  @Override
  protected void performFinish() {
    // Shutdown sequence: Multicast finish event silently
    if (systemComponents != null) {
      systemComponents.multicastSilently()
          .finish();
      log.debug("Finished all system components.");

      // Unregister all system components
      this.systemComponents.clear();
    }
    log.debug("System components unregistered.");
  }

}
