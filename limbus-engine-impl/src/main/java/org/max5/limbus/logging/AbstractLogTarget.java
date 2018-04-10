package org.max5.limbus.logging;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import org.max5.limbus.Initializable;
import org.max5.limbus.launcher.EngineLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract implementation of a {@link LogTarget} used by the Limbus Engine to separate logging output of
 * plugins and write it separately to any registered destination. This abstract implementation provides the general
 * lifecycle management and the call mapping.
 *
 *
 * @param <T>
 *        The type of the underlying write target.
 *
 * @author schuettec
 *
 */
public abstract class AbstractLogTarget<T> extends Initializable<Exception> implements LogTarget {

  private static final Logger log = LoggerFactory.getLogger(AbstractLogTarget.class);

  protected RoutedOutputStream<Integer, T> routerStdOut;
  protected RoutedOutputStream<Integer, T> routerStdErr;

  private ThreadSensitiveRecorderStream recorderStdOut;
  private ThreadSensitiveRecorderStream recorderStdErr;

  private MaintenanceFilterStream filterStdOut;
  private MaintenanceFilterStream filterStdErr;

  @Override
  public void openChannel(ClassLoader classLoader, String deployName) {
    checkState();
    int classLoaderHashCode = ContextClassloaderSelector.getClassLoaderHashCode(classLoader);
    {
      T target = createStdOutTarget(deployName);
      this.routerStdOut.addTarget(classLoaderHashCode, target);
    }
    {
      T target = createStdErrTarget(deployName);
      this.routerStdErr.addTarget(classLoaderHashCode, target);
    }
  }

  @Override
  public void recordChannel(ClassLoader classLoader, Thread thread, ByteArrayOutputStream stdOutTarget,
      ByteArrayOutputStream stdErrTarget) {
    ClassloaderThreadID id = ClassloaderThreadID.getID(classLoader, thread);
    if (hasRecorderSubscriber(id)) {
      throw new UnsupportedOperationException(
          "The specified classloader and thread context are already recorded by a subscriber.");
    } else {
      recorderStdOut.addSubscriber(id, stdOutTarget);
      recorderStdErr.addSubscriber(id, stdErrTarget);
    }
  }

  private boolean hasRecorderSubscriber(ClassloaderThreadID id) {
    return (recorderStdOut.hasSubscriber(id) || recorderStdErr.hasSubscriber(id));
  }

  @Override
  public boolean isRecordingChannel(ClassLoader classLoader, Thread thread) {
    ClassloaderThreadID id = ClassloaderThreadID.getID(classLoader, thread);
    return hasRecorderSubscriber(id);
  }

  @Override
  public ByteArrayOutputStream[] stopRecordChannel(ClassLoader classLoader, Thread thread) {
    ClassloaderThreadID id = ClassloaderThreadID.getID(classLoader, thread);
    if (hasRecorderSubscriber(id)) {
      ByteArrayOutputStream stdOut = recorderStdOut.getSubscriber(id);
      recorderStdOut.removeSubscriber(id);
      ByteArrayOutputStream stdErr = recorderStdErr.getSubscriber(id);
      recorderStdErr.removeSubscriber(id);
      return new ByteArrayOutputStream[] {
          stdOut, stdErr
      };
    } else {
      throw new NoSuchElementException("No recording for the specified classloader and thread context.");
    }
  }

  /**
   * Called by the {@link AbstractLogTarget} to get the default write target for std/err. All calls that cannot be
   * identified using
   * the {@link ContextClassloaderSelector} are mapped to this default target.
   *
   * @return Returns the default target to write calls to, that cannot be identified.
   */
  protected abstract T getDefaultTargetStdErr();

  @Override
  public void closeChannel(ClassLoader classLoader) {
    checkState();
    int classLoaderHashCode = ContextClassloaderSelector.getClassLoaderHashCode(classLoader);
    closeChannel(routerStdOut, classLoaderHashCode);
    closeChannel(routerStdErr, classLoaderHashCode);
  }

  /**
   * Called to create a new write target for the std/out channel. Implementations can use the deploy name to
   *
   * @param deployName
   *        The deploy name the target is assigned to.
   * @return Returns the target.
   */
  protected abstract T createStdOutTarget(String deployName);

  /**
   * Called to create a new write target for the std/err channel. Implementations can use the deploy name to
   *
   * @param deployName
   *        The deploy name the target is assigned to.
   * @return Returns the target.
   */
  protected abstract T createStdErrTarget(String deployName);

  /**
   * @param target
   *        Destroys the target.
   */
  protected abstract void destroyTarget(T target);

  /**
   * Called by the {@link AbstractLogTarget} to get the target writer. Implementations may return a new created and non
   * cached instance. This method is only called once.
   *
   * @return Returns the target writer to write log calls to. May not be <code>null</code>.
   */
  protected abstract TargetWriter<T> getTargetWriter();

  /**
   * Called by the {@link AbstractLogTarget} to get the default write target for std/out. All calls that cannot be
   * identified using
   * the {@link ContextClassloaderSelector} are mapped to this default target.
   *
   * @return Returns the default target to write calls to, that cannot be identified.
   */
  protected abstract T getDefaultTargetStdOut();

  private void closeChannel(RoutedOutputStream<Integer, T> routedOutput, int contextHashCode) {
    T target = routedOutput.getTarget(contextHashCode);
    destroyTarget(target);
    routedOutput.removeTarget(contextHashCode);
  }

  @Override
  protected void performInitialize() throws Exception {
    this.routerStdOut = new RoutedOutputStream<Integer, T>(new ContextClassloaderSelector<T>(), getTargetWriter(),
        getDefaultTargetStdOut());
    this.routerStdErr = new RoutedOutputStream<Integer, T>(new ContextClassloaderSelector<T>(), getTargetWriter(),
        getDefaultTargetStdErr());

    this.recorderStdOut = new ThreadSensitiveRecorderStream(routerStdOut);
    this.recorderStdErr = new ThreadSensitiveRecorderStream(routerStdErr);

    // TODO - schuettec - 21.10.2016 : This is not a good but working solution. Selecting targets with more than one
    // condition is not possible for the moment. Selecting target writers is not possible too. To redirect the
    // Limbus Maintenance Console to original std/out, this is the only working solution without changing the
    // architecture.
    this.filterStdOut = new MaintenanceFilterStream(recorderStdOut);
    this.filterStdErr = new MaintenanceFilterStream(recorderStdErr);

    // Redirect system out
    EngineLauncher.redirectSystemOut(filterStdOut);
    EngineLauncher.redirectSystemError(filterStdErr);
  }

  @Override
  protected void performFinish() {
    flushOnDemand(filterStdOut);
    flushOnDemand(filterStdErr);

    // Reset System.out to original state
    EngineLauncher.resetSystemOut();
    EngineLauncher.resetSystemError();

    // Finish the filter and indirectly finish the router (the close is delegated)
    closeAndClear(filterStdOut);
    closeAndClear(filterStdErr);

  }

  private void closeAndClear(OutputStream output) {
    if (output != null) {
      try {
        output.close();
      } catch (Exception e) {
        log.warn("Cannot close logging router due to an exception.", e);
      }
    }
  }

  private void flushOnDemand(OutputStream output) {
    try {
      if (output != null) {
        output.flush();
      }
    } catch (Exception e) {
      log.warn("Cannot flush logging router due to an exception.", e);
    }
  }

}
