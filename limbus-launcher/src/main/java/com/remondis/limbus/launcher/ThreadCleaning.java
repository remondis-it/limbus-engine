package com.remondis.limbus.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.utils.Lang;

/**
 * This class encapsulates the thread cleaning and analysis features of the Limbus Engine.
 *
 * 
 *
 */
public class ThreadCleaning {

  private static final Logger log = LoggerFactory.getLogger(ThreadCleaning.class);

  public static final int THREAD_WAIT_TIMEOUT = 1000;

  private static final List<String> STATIC_THREADGROUP_FILTER = new ArrayList<>();
  private static final List<String> STATIC_THREAD_FILTER = new ArrayList<>();

  static {
    STATIC_THREADGROUP_FILTER.add("RMI Runtime");
    STATIC_THREADGROUP_FILTER.add("system");

    STATIC_THREAD_FILTER.add("AWT-EventQueue.*");
  }

  /**
   * @return Returns a list of all threads currently known by the JVM.
   */
  protected static Set<Thread> getAllThreads() {
    Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
    Set<Thread> threadSet = allStackTraces.keySet();
    return new HashSet<>(threadSet);
  }

  /**
   * Waits the specified amount of milliseconds to give the thread a chance to terminate. If the timeout is reached this
   * method tries to finish the specified thread using an interrupt.
   *
   * <p>
   * <b>Note that the specified time is applied twice on worst case.</b>
   * </p>
   *
   * <p>
   * <b>
   * The timeout approach was chosen because the shutdown sequence is so fast that it does not give threads enough time
   * to terminate completely.
   * </b>
   * </p>
   *
   * @param dryRun
   *        If <code>true</code> this method does not really interrupt threads.
   * @param thread
   *        The thread to finish.
   * @return Returns <code>true</code> if the thread could be terminated using interrupts, <code>false</code> if the
   *         thread could NOT be terminated using interrupt and stop() called to terminate the thread.
   *
   */
  @SuppressWarnings("deprecation") // We use Thread.stop() here to avoid that the application becomes a non-terminating
                                   // daemon process if a thread does not terminate.
  private static boolean finishThread(boolean dryRun, Thread thread, int joinMillisTimeout) {
    if (thread.isAlive()) {
      // Give the thread some time to terminate gently.
      try {
        thread.join(joinMillisTimeout);
      } catch (InterruptedException e) {
        // Nothing to do here.
      }
    }

    if (thread.isAlive()) {

      if (dryRun) {
        log.warn("There is an unstopped thread - will not terminate it, this is a dry-run! Thread was named '{}'",
            thread.getName());
        return false;
      } else {
        log.warn("There is an unstopped thread - trying to terminate '{}'", thread.getName());
        try {
          thread.interrupt();
        } catch (Exception e) {
          // Nothing to do here.
        }

        // Give the thread some time to die
        try {
          thread.join(joinMillisTimeout);
        } catch (InterruptedException e) {
          // Nothing to do here.
        }

        if (thread.isAlive()) {
          String stackTraceStr = Lang.stackTraceAsString(thread.getStackTrace());
          log.warn("The thread '{}' was requested to terminate but did not respond.", thread.getName());
          log.warn("Stacktrace of the thread '{}' in thread group '{}':\n{}", thread.getName(), thread.getThreadGroup()
              .getName(), stackTraceStr);
          log.warn("Stopping the thread '{}' with stop().", thread.getName());

          thread.stop();
          return false;
        } else {
          return true;
        }

      }
    } else {
      return true;
    }

  }

  /**
   * This method checks if a thread should be processed by thread cleaning features.
   *
   * @param thread
   *        The thread to check
   * @param threadSnapshot
   *        Threads recorded by a thread snapshot are ignored by this method.
   * @param noProcessThreads
   *        The threads to be ignored.
   * @return Returns <code>true</code> if the thread should be processed by thread cleaning features or
   *         <code>false</code> if the thread should not be processed either because it is a JVM system thread or the
   *         thread is contained by the <tt>noProcessThreads</tt> list.
   */
  public static boolean processThread(Thread thread, ThreadSnapshot threadSnapshot, List<Thread> noProcessThreads) {
    // schuettec - 25.04.2017 : Ignore Daemon Threads because they will terminate if no other thread lives
    if (thread.isDaemon()) {
      return false;
    }

    ThreadGroup threadGroup = thread.getThreadGroup();
    // Filter the shutdown thread and other java system threads
    if (noProcessThreads.contains(thread) || thread.getName()
        .equals("DestroyJavaVM") || (threadGroup != null && STATIC_THREADGROUP_FILTER.contains(threadGroup.getName()))
        || isStaticFilteredThreadName(thread.getName())) {
      return false;
    } else if (threadSnapshot != null && threadSnapshot.contains(thread)) {
      return false;
    } else {
      return true;
    }
  }

  private static boolean isStaticFilteredThreadName(String threadName) {
    Iterator<String> it = STATIC_THREAD_FILTER.iterator();
    while (it.hasNext()) {
      String threadPattern = it.next();
      if (threadName.matches(threadPattern)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method checks if a thread should be processed by thread cleaning features.
   *
   * @param thread
   *        The thread to check
   * @param threadSnapshot
   *        Threads recorded by a thread snapshot are ignored by this method.
   * @param noProcessThreads
   *        The threads to be ignored.
   * @return Returns <code>true</code> if the thread should be processed by thread cleaning features or
   *         <code>false</code> if the thread should not be processed either because it is a JVM system thread or the
   *         thread is contained by the <tt>noProcessThreads</tt> list.
   */
  public static boolean processThread(Thread thread, ThreadSnapshot threadSnapshot, Thread... noProcessThreads) {
    if (noProcessThreads == null) {
      noProcessThreads = new Thread[0];
    }
    return processThread(thread, threadSnapshot, Arrays.asList(noProcessThreads));

  }

  /**
   * Tries to finish all running user threads. JVM threads are filtered and will be ignored. Uses the default maximal
   * time to spend on joining threads {@link #THREAD_WAIT_TIMEOUT} (can apply two times!).
   *
   * @param threadSnapshot
   *        Threads recorded by a thread snapshot are ignored by this method.
   * @param filtered
   *        The threads that should be ignored.
   * @return Returns <code>true</code> if one of the threads could not be finished using an interrupt.
   *         <code>false</code>
   *         is returned if all threads terminated cleanly.
   *
   */
  public static boolean finishRunningThreads(ThreadSnapshot threadSnapshot, Thread... filtered) {
    return finishRunningThreads(THREAD_WAIT_TIMEOUT, threadSnapshot, filtered);

  }

  /**
   * Tries to finish all running user threads. JVM threads are filtered and will be ignored.
   *
   * @param maxTimeoutMillis
   *        maximal time to spend on joining threads (can apply two times!).
   * @param threadSnapshot
   *        Threads recorded by a thread snapshot are ignored by this method.
   * @param filtered
   *        The threads that should be ignored.
   * @return Returns <code>true</code> if on of the threads could not be finished using an interrupt. <code>false</code>
   *         is returned if all threads terminated cleanly.
   *
   */
  public static boolean finishRunningThreads(int maxTimeoutMillis, ThreadSnapshot threadSnapshot, Thread... filtered) {
    boolean wasDirty = finishRunningThreads(false, maxTimeoutMillis, threadSnapshot, filtered);

    return wasDirty;
  }

  /**
   * Tries to finish all running user threads. JVM threads are filtered and will be ignored. Uses the default maximal
   * time to spend on joining threads {@link #THREAD_WAIT_TIMEOUT} (can apply two times!).
   *
   * @param dryRun
   *        If <code>true</code> this method does not really interrupt threads.
   * @param threadSnapshot
   *        Threads recorded by a thread snapshot are ignored by this method.
   * @return Returns <code>true</code> if on of the threads could not be finished using an interrupt. <code>false</code>
   *         is returned if all threads terminated cleanly.
   *
   */
  public static boolean finishRunningThreads(boolean dryRun, ThreadSnapshot threadSnapshot) {
    return finishRunningThreads(dryRun, THREAD_WAIT_TIMEOUT, threadSnapshot);
  }

  /**
   * Tries to finish all running user threads. JVM threads are filtered and will be ignored. Uses the default maximal
   * time to spend on joining threads {@link #THREAD_WAIT_TIMEOUT} (can apply two times!).
   *
   * @param dryRun
   *        If <code>true</code> this method does not really interrupt threads.
   * @param threadSnapshot
   *        Threads recorded by a thread snapshot are ignored by this method.
   * @param filtered
   *        The threads that should be ignored.
   * @return Returns <code>true</code> if on of the threads could not be finished using an interrupt. <code>false</code>
   *         is returned if all threads terminated cleanly.
   *
   */
  public static boolean finishRunningThreads(boolean dryRun, ThreadSnapshot threadSnapshot, Thread... filtered) {
    return finishRunningThreads(dryRun, THREAD_WAIT_TIMEOUT, threadSnapshot, filtered);
  }

  /**
   * Tries to finish all running user threads. JVM threads are filtered and will be ignored.
   *
   * @param dryRun
   *        If <code>true</code> this method does not really interrupt threads.
   * @param maxTimeoutMillis
   *        maximal time to spend on joining threads (can apply two times!).
   * @param threadSnapshot
   *        Threads recorded by a thread snapshot are ignored by this method.
   * @param filtered
   *        The threads that should be ignored.
   * @return Returns <code>true</code> if one of the threads could not be finished using an interrupt.
   *         <code>false</code>
   *         is returned if all threads terminated cleanly.
   *
   */
  public static boolean finishRunningThreads(boolean dryRun, int maxTimeoutMillis, ThreadSnapshot threadSnapshot,
      Thread... filtered) {
    if (filtered == null) {
      filtered = new Thread[0];
    }
    List<Thread> noProcessThreads = Arrays.asList(filtered);

    log.info("Checking for abandoned threads...");

    boolean wasDirty = false;

    Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
    Set<Thread> threadSet = allStackTraces.keySet();
    Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
    for (Thread thread : threadArray) {
      if (processThread(thread, threadSnapshot, noProcessThreads)) {
        wasDirty = wasDirty || !finishThread(dryRun, thread, maxTimeoutMillis);
      }
    }

    if (wasDirty) {
      log.warn("There were unstopped abandoned threads - see log above.");
    } else {
      log.info("Engine was shut down cleanly - no abandoned threads.");
    }
    return wasDirty;
  }

}
