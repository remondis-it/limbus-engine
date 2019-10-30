package com.remondis.limbus.launcher;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.utils.Lang;

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
public abstract class AbstractEngine extends Initializable<Exception> implements Engine {
  private static final Logger log = LoggerFactory.getLogger(AbstractEngine.class);

  public static final long SHUTDOWN_TIMEOUT = TimeUnit.MINUTES.toMillis(3);
  public static final int OBSERVER_FREQUENCY = 1000;

  private static final String THREAD_TITLE = "Engine Observer";

  protected volatile boolean wasStopped;
  protected Thread observer;

  public AbstractEngine() {
  }

  @Override
  public final void startEngine() throws Exception {
    // Start engine observer
    startEngineObserver();

    // Initialize engine
    initialize();
  }

  private void startEngineObserver() {
    this.observer = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!wasStopped) {
          try {
            Thread.sleep(OBSERVER_FREQUENCY);
          } catch (InterruptedException e) {
            // Maybe we interrupt ourselves but then stopEngine() was called and the next loop starts the shutdown
            // sequence.
          }
        }

        // Finish the engine
        finish();
      }
    });
    this.observer.setName(THREAD_TITLE);
    this.observer.setPriority(Thread.MIN_PRIORITY);
    this.observer.start();
  }

  @Override
  public final void stopEngine() {
    wasStopped = true;
    if (this.observer != null) {
      try {
        this.observer.interrupt();
        this.observer.join(SHUTDOWN_TIMEOUT);
      } catch (InterruptedException e) {
        // Nothing to do
      } finally {
        if (this.observer.isAlive()) {
          StackTraceElement[] stackTrace = this.observer.getStackTrace();
          String stackTraceStr = Lang.stackTraceAsString(stackTrace);
          log.warn("Engine observer thread could not be terminated:\n{}", stackTraceStr);
        }
        this.observer = null;
      }
    }
  }

}
