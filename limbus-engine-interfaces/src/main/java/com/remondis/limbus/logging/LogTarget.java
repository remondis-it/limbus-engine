package com.remondis.limbus.logging;

import java.io.ByteArrayOutputStream;

import com.remondis.limbus.IInitializable;

/**
 * This interface describes a log target.
 *
 * @author schuettec
 *
 */
public interface LogTarget extends IInitializable<Exception> {

  /**
   * Creates a logging channel for the specified deploy name.
   *
   * @param classLoader
   *        The context classloader that is used to separate the log calls.
   * @param deployName
   *        The deploy name name the logging channel, <b>may not be <code>null</code>.</b>
   */
  void openChannel(ClassLoader classLoader, String deployName);

  /**
   * Records the std/out and std/err output of a plugin's thread with the specified {@link ByteArrayOutputStream}s.
   * Use {@link ByteArrayOutputStream}s to be able to retrieve the data. <b>Note: There must be an open channel for
   * the specified classloader context.</b>
   *
   * @param classLoader
   *        The classloader to identify std/out/err access.
   * @param thread
   *        The thread to identify std/out/err access.
   * @param stdOutTarget
   *        The delegate recording stream for std/out
   * @param stdErrTarget
   *        The delegate recording stream for std/err
   */
  public void recordChannel(ClassLoader classLoader, Thread thread, ByteArrayOutputStream stdOutTarget,
      ByteArrayOutputStream stdErrTarget);

  /**
   * Checks if there is already a recording running for the specified classloader and thread.
   *
   * @param classLoader
   *        The classloader
   * @param thread
   *        The thread
   * @return Returns <code>true</code> if the std/out/err channels are already recorded, <code>false</code> otherwise.
   */
  public boolean isRecordingChannel(ClassLoader classLoader, Thread thread);

  /**
   * Stops recording of the specified classloader and thread context.
   *
   * @param classLoader
   *        The classloader
   * @param thread
   *        The thread
   * @return Returns an array containing the std/out delegate stream as first element and the std/err delegate stream
   *         as second element. To retrieve the data the stream will be returned as {@link ByteArrayOutputStream}.
   *         <b>Note: Close the streams after receiving them through this method.</b>
   */
  public ByteArrayOutputStream[] stopRecordChannel(ClassLoader classLoader, Thread thread);

  /**
   * Closes a previously created logging channel for the specified deploy name. <b>Implementations may not throw an
   * exception if the deploy name does not exist. Just do nothing if the deploy name is not known.</b>
   *
   * @param classLoader
   *        The context classloader that is used to separate the log calls.
   *
   */
  void closeChannel(ClassLoader classLoader);

}
