package com.remondis.limbus.launcher;

/**
 * A thread filter is used by the {@link ThreadSnapshot} to filter known threads that should not be part of a
 * {@link ThreadSnapshot}.
 *
 * 
 *
 */
public interface ThreadFilter {

  /**
   * Called by the {@link ThreadSnapshot} to determine if this thread should be part of the snapshot.
   *
   * @param thread
   *        The thread to check
   * @return Returns <code>true</code> if the thread should be recorded by the {@link ThreadSnapshot},
   *         <code>false</code> otherwise.
   */
  boolean accept(Thread thread);

}
