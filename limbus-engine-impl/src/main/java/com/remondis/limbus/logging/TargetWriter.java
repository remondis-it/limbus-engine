package com.remondis.limbus.logging;

/**
 * A {@link TargetWriter} implements calls to the write methods of {@link RoutedOutputStream} by converting the data
 * format of an {@link OutputStream} to convenient calls of the underlying target type.
 *
 * @param <T>
 *        The type of the underlying write target.
 * @author schuettec
 *
 */
public interface TargetWriter<T> {

  /**
   * @param b
   *        the data.
   * @param target
   *        The write target.
   * @throws TargetWriteException
   *         Thrown on any target error.
   */
  public void writeTo(int b, T target) throws TargetWriteException;

  /**
   * Writes data to the specified target.
   *
   * @param b
   *        the data.
   * @param off
   *        the start offset in the data.
   * @param len
   *        the number of bytes to write.
   * @param target
   *        The write target.
   * @throws TargetWriteException
   *         Thrown on any target error.
   */
  public void writeTo(byte[] b, int off, int len, T target) throws TargetWriteException;

  /**
   * Flushes any buffers of the underlying write target
   *
   * @param target
   *        The target to flush
   * @throws TargetWriteException
   *         Thrown on any target error.
   */
  public void flush(T target) throws TargetWriteException;

  /**
   * Closes the underlying write target silently.
   *
   * @param target
   *        The target to close.
   * @throws TargetWriteException
   *         Thrown on any target error.
   */
  public void close(T target) throws TargetWriteException;

}
