package com.remondis.limbus.launcher;

/**
 * This interface defines the lifecycle methods for the {@link Engine} running as a service daemon. On engine startup
 * {@link #startEngine()} is called by the framework. When the engine is requested to stop via external signals
 * {@link #stopEngine()} is called.
 *
 * @author schuettec
 *
 */
public interface Engine {

  /**
   * Called by the {@link EngineLauncher} to launch the engine instance.
   *
   * @throws Exception
   *         Thrown on any error.
   */
  public void startEngine() throws Exception;

  /**
   * Called by the {@link EngineLauncher} to stop the engine instance. This method is assumed to block until the engine
   * is stopped. Implementations may force the shutdown.
   */
  public void stopEngine();
}
