package com.remondis.limbus.utils;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StopWatch implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static boolean USE_SYSTEM_OUT = false;

  private static final Map<String, StopWatch> StopWatchs = new Hashtable<String, StopWatch>();

  public static void unregisterGlobalStopWatch(String id) {
    StopWatchs.remove(id);
  }

  public static void registerGlobalStopWatch(String id, StopWatch b) {
    StopWatchs.put(id, b);
  }

  public static StopWatch getGlobalStopWatch(String id) {
    return StopWatchs.get(id);
  }

  protected long nanoStart;
  protected long nanoEnd;

  protected boolean StopWatchRunning;
  protected boolean StopWatchReady;

  protected String title;
  protected String description;

  private String className;

  private String method;

  private int line;

  public StopWatch() {
    // schuettec - 19.04.2017 : The constructor call chain is not used here, because this would change the stack frames
    // and getting the caller information would be difficult.
    getCallerInformation();
    this.title = String.format("%s.%s() - (Line %s)", className, method, line);
  }

  /**
   * @param title
   */
  public StopWatch(final String title) {
    // schuettec - 19.04.2017 : The constructor call chain is not used here, because this would change the stack frames
    // and getting the caller information would be difficult.
    getCallerInformation();
    this.title = title;
    this.description = "No description";
  }

  /**
   * @param title
   * @param description
   */
  public StopWatch(final String title, final String description) {
    // schuettec - 19.04.2017 : The constructor call chain is not used here, because this would change the stack frames
    // and getting the caller information would be difficult.
    getCallerInformation();
    this.title = title;
    this.description = description;
  }

  private void getCallerInformation() {
    StackTraceElement[] stackTraceElements = Thread.currentThread()
        .getStackTrace();
    this.className = stackTraceElements[3].getClassName();
    this.method = stackTraceElements[3].getMethodName();
    this.line = stackTraceElements[3].getLineNumber();
  }

  /**
   * @return the className
   */
  public String getClassName() {
    return className;
  }

  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * @return the line
   */
  public int getLine() {
    return line;
  }

  public void start() {
    if (this.StopWatchRunning) {
      throw new IllegalStateException("StopWatch already running.");
    }
    this.StopWatchReady = false;
    this.StopWatchRunning = true;
    this.nanoStart = System.nanoTime();
  }

  public void stop() {
    if (!this.StopWatchRunning) {
      throw new IllegalStateException("StopWatch not running. Start it before stopping.");
    }
    this.nanoEnd = System.nanoTime();
    this.StopWatchRunning = false;
    this.StopWatchReady = true;
  }

  public long getNanoRuntime() {
    if (!this.StopWatchReady) {
      throw new IllegalStateException("StopWatch was not performed!");
    }
    return this.nanoEnd - this.nanoStart;
  }

  public long getMillisecondsRuntime() {
    return TimeUnit.NANOSECONDS.toMillis(this.getNanoRuntime());
  }

  public String getFormattedDifference() {
    try {
      return String.format("Runtime in ms: %-5d for %s", TimeUnit.NANOSECONDS.toMillis(this.getNanoRuntime()),
          this.title);
    } catch (final IllegalStateException e) {
      return this.title + " is still running.";
    }
  }

  public String getStopWatchInformation() {
    return this.getFormattedDifference() + ((description == null) ? "" : (" - " + this.description));
  }

  @Override
  public String toString() {
    return this.getStopWatchInformation();
  }

  public String getTitle() {
    return this.title;
  }

  public String getDescription() {
    return this.description;
  }

  public long getNanoStart() {
    return this.nanoStart;
  }

  public long getNanoEnd() {
    return this.nanoEnd;
  }

  public boolean isStopWatchRunning() {
    return StopWatchRunning;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}