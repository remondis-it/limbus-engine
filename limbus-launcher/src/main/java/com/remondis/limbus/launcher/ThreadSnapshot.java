package com.remondis.limbus.launcher;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to take a snapshot of the currently running threads. The thread cleaning feature of
 * {@link EngineLauncher} will then ignore these threads while shutting down.
 *
 * @author schuettec
 *
 */
public class ThreadSnapshot {

  private Set<Integer> systemHashCodes;

  private ThreadSnapshot(Set<Integer> hashCodes) {
    this.systemHashCodes = hashCodes;
  }

  /**
   * Checks if the specified thread is part of this thread snapshot.
   *
   * @param thread
   *        The thread to check
   * @return Returns <code>true</code> if the specified thread is known by this {@link ThreadSnapshot} otherwise
   *         <code>false</code> is returned.
   */
  public boolean contains(Thread thread) {
    return systemHashCodes.contains(System.identityHashCode(thread));
  }

  public int size() {
    return systemHashCodes.size();
  }

  public boolean isEmpty() {
    return systemHashCodes.isEmpty();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((systemHashCodes == null) ? 0 : systemHashCodes.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ThreadSnapshot other = (ThreadSnapshot) obj;
    if (systemHashCodes == null) {
      if (other.systemHashCodes != null)
        return false;
    } else if (!systemHashCodes.equals(other.systemHashCodes))
      return false;
    return true;
  }

  /**
   * Clears the recorded thread snapshot.
   */
  public void clear() {
    systemHashCodes.clear();
  }

  /**
   * @return Returns the thread objects from this snapshot if they are still known by the JVM.
   */
  public Set<Thread> getThreads() {
    Set<Thread> allThreads = new HashSet<>();
    for (Thread t : ThreadCleaning.getAllThreads()) {
      int identityHashCode = System.identityHashCode(t);
      if (systemHashCodes.contains(identityHashCode)) {
        allThreads.add(t);
      }
    }
    return allThreads;
  }

  /**
   * Determines the threads that were known by the specified snapshot. The threads known by the specified snapshot that
   * are not recorded by this snapshot will be returned.
   *
   * @param snapshot
   *        The snapshot to compare with.
   * @return Returns the set of threads that are known by the specified snapshot and were not recorded by this snapshot.
   */
  public Set<Integer> difference(ThreadSnapshot snapshot) {
    Set<Integer> difference = new HashSet<>();
    // snapshot is assumed to be the latest thread snapshot. We want to know which threads were created later so the
    // difference is NOW - BEFORE = threads created after taking this snapshot.
    difference.addAll(snapshot.systemHashCodes);
    // Remove all thread ids recorded by this thread snapshot before
    difference.removeAll(systemHashCodes);
    return difference;
  }

  /**
   * @return Returns a new {@link ThreadSnapshot} containing the currently active threads. JVM system threads are
   *         filtered and not part of the thread snapshot.
   * @see #snapshot(boolean)
   */
  public static ThreadSnapshot snapshot() {
    return snapshot(true);
  }

  /**
   * @param filterJVMThreads
   *        If <code>true</code> the JVM system threads are not part of the created snapshot. If <code>false</code>
   *        also JVM system threads will be recorded by the thread snapshot.
   * @return Returns a new {@link ThreadSnapshot} containing the currently active threads.
   */
  public static ThreadSnapshot snapshot(boolean filterJVMThreads) {
    return snapshot(filterJVMThreads, null);
  }

  /**
   * @param filterJVMThreads
   *        If <code>true</code> the JVM system threads are not part of the created snapshot. If <code>false</code>
   *        also JVM system threads will be recorded by the thread snapshot.
   * @param filterCondition
   *        (Optional) An optional filter condition
   * @return Returns a new {@link ThreadSnapshot} containing the currently active threads.
   */
  public static ThreadSnapshot snapshot(boolean filterJVMThreads, ThreadFilter filterCondition) {
    Set<Integer> hashCodes = new HashSet<>();
    Set<Thread> allThreads = ThreadCleaning.getAllThreads();
    for (Thread t : allThreads) {
      boolean add = true;
      if (filterJVMThreads) {
        add = ThreadCleaning.processThread(t, null);
      }
      if (add) {
        if (filterCondition != null) {
          add = filterCondition.accept(t);
        }
        if (add) {
          hashCodes.add(System.identityHashCode(t));
        }
      }
    }
    return new ThreadSnapshot(hashCodes);
  }

  /**
   * Tries to find a thread with the specified id.
   *
   * @param id
   *        The thread id from a {@link ThreadSnapshot} object.
   * @return Returns the thread if it is currently active/known by the JVM, or <code>null</code> if the thread has been
   *         terminated.
   */
  public static Thread getThread(Integer id) {
    Set<Thread> allThreads = ThreadCleaning.getAllThreads();
    for (Thread t : allThreads) {
      if (System.identityHashCode(t) == id) {
        return t;
      }
    }
    return null;
  }
}
