/**
 *
 */
package com.remondis.limbus;

import java.util.concurrent.Semaphore;

import com.remondis.limbus.exceptions.AlreadyInitializedException;
import com.remondis.limbus.exceptions.NotInitializedException;

/**
 * This class offers an implementation of {@link IInitializable}. It is used for objects that have to be initialized
 * before calling further operations. This class provides methods for checking the state, perform the init- and
 * deinitialization.
 *
 * <p>
 * <b> This class is thread safe on initialization and deinitialization. </b>
 * </p>
 * <p>
 * <b> Note: Implementations may not call their own lifecycle methods like {@link #initialize()}, {@link #finish()},
 * {@link #performInitialize()} or {@link #performFinish()}! To abort the initialization, simply throw an exception.
 * Then the initialization is aborted and the finish operation is executed. </b>
 * </p>
 *
 *
 * @param <E>
 *        The type of the business exception the implementation may throw during initialization.
 * @author schuettec
 *
 */
public abstract class Initializable<E extends Exception> implements IInitializable<E> {

  private transient Object lock = new Object();
  private transient boolean initialized;

  /**
   *
   */
  public Initializable() {
    readResolve();
  }

  /**
   * Initializes this object. This method is thread safe.
   *
   * @throws E
   *         Thrown if the initialization failed.
   */
  @Override
  public void initialize() throws E {
    synchronized (lock) {
      if (initialized) {
        throw new AlreadyInitializedException(
            "This object was already initialized. It is highly recommended to not call initialize multiple times during the lifecycle.");
      }

      // Set initialized flag to true, because some implementations use their own operations within
      // initialization.
      this.initialized = true;
      try {
        performInitialize();
      } catch (Exception e) {
        // On exception finish this object
        finish();
        throw e;
      }
    }
  }

  /**
   * Use this method to ensure on business operations that this object has been initialized before.
   *
   * @throws NotInitializedException
   *         Thrown if this object was not initialized.
   */
  protected void checkState() throws NotInitializedException {
    synchronized (lock) {
      if (!initialized) {
        throw new NotInitializedException(
            "This object is not initialized. Call initialize() before performing further operations.");
      }
    }
  }

  /**
   * This method deinitializes this object.
   */
  @Override
  public void finish() {
    synchronized (lock) {
      if (initialized) {
        try {
          performFinish();
        } catch (Throwable e) {
          // Do not use a logger here. Logging would require dependencies that will present at classloading
          // time, but
          // not
          // when running in a plugin's runtime context.
          new Exception("The performFinish() operation was expected to be silent but threw an exception.", e)
              .printStackTrace();
        } finally {
          initialized = false;
        }
      }
    }
  }

  /**
   * Performs the acutal initialization of subclasses. Implement this method for your own business.
   *
   * <p>
   * <b>Note: This method is thread safe.</b>
   * </p>
   *
   * @throws E
   *         Thrown to signal that the initialization fails. If thrown this object remains uninitialized.
   */
  protected abstract void performInitialize() throws E;

  /**
   * Performs the silent deinitialization. Implement this method for your own business.
   *
   * <p>
   * <b>Note: This method is thread safe.</b>
   * </p>
   */
  protected abstract void performFinish();

  protected Object readResolve() {
    this.lock = new Semaphore(1);
    this.initialized = false;
    return this;
  }
}
