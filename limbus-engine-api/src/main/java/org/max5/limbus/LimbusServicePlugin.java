package org.max5.limbus;

/**
 * This abstract class implements a service plugin for use with the runtime engine. A {@link LimbusServicePlugin} is
 * basically an {@link IInitializable} type so it can be added as system component to the runtime engine. The plugin
 * manages a thread that performs the business logic of the service. On any termination signal from the environment the
 * thread is requested to terminate and the business logic is called to shutdown this service plugin.
 *
 * <h2>Lifecycle</h2>
 * <p>
 * <b> Implementors should not use infinite loops or constructs that can prevent threads from terminating. Make use of
 * the method #wasStopped() to determine the current service plugin state. </b>
 * </p>
 * <p>
 * The runtime framework will take care of the lifecycles of system components like {@link LimbusServicePlugin}.
 * Therefore it is not recommended to join the started threads created in a plugin. This implementation tries on best
 * effort to terminate the local thread on any interrupt or termination signal.
 * </p>
 * <p>
 * <b>Note: Everytime an {@link InterruptedException} is caught implementors must set the interrupt-flag using
 * {@link Thread#interrupt()} to safely terminate. </b>
 * </p>
 *
 * @author schuettec
 *
 */
public abstract class LimbusServicePlugin extends Initializable<Exception> implements LimbusPlugin, Runnable {

  private Thread pluginThread;

  public LimbusServicePlugin() {
    pluginThread = new Thread(this, "ServicePlugin - " + getClass().getSimpleName());
  }

  @Override
  public void initialize() throws Exception {
    super.initialize();

    this.pluginThread.start();
  }

  /**
   * @return Returns <code>true</code> if this service plugin may continue running, otherwise <code>false</code>is
   *         returned. Then the service plugin should immediately stop running.
   */
  protected boolean isRunning() {
    return !pluginThread.isInterrupted();
  }

  @Override
  public void finish() {
    this.pluginThread.interrupt();
    try {
      this.pluginThread.join();
    } catch (InterruptedException e) {
      // Do not use a logger here. Logging would require dependencies that will present at classloading time, but
      // not
      // when running in a plugin's runtime context.
      new Exception(String.format(
          "The thread of this limbus service plugin (%s) was requested to stop but didn't respond in the allowed time.",
          this.getClass()
              .getName())).printStackTrace();
    }

    super.finish();
  }

}
