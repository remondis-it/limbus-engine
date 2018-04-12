package com.remondis.limbus.monitoring;

import java.util.concurrent.TimeUnit;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("processing")
public class ProcessingConfig {

  protected static final int DEFAULT_MAX_THREADS = 4;

  protected static final int DEFAULT_MAX_PENDING_JOBS = 4;

  protected static final int DEFAULT_CORE_THREADS = 2;

  protected static final int DEFAULT_KEEP_ALIVE = 30;

  protected static final TimeUnit DEFAULT_KEEP_ALIVE_UNIT = TimeUnit.SECONDS;

  protected static final Integer DEFAULT_TIMEOUT = 60;

  protected static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

  /**
   * Holds the maximum amount of threads the thread pool may hold at any given time.
   */
  protected Integer maxThreads = DEFAULT_MAX_THREADS;
  /**
   * Holds the number of threads that will be available permanently.
   */
  protected Integer coreThreads = DEFAULT_CORE_THREADS;

  /**
   * Holds the time unit of the field threadKeepAlive
   */
  protected TimeUnit threadKeepAliveUnit = DEFAULT_KEEP_ALIVE_UNIT;
  /**
   * Holds the time additional threads (additional to the core threads count) wait until terminating.
   */
  protected Integer threadKeepAlive = DEFAULT_KEEP_ALIVE;

  /**
   *
   * Holds the time unit of the field threadKeepAlive
   */
  protected TimeUnit timeoutUnit = DEFAULT_TIMEOUT_UNIT;
  /**
   * Holds the time additional threads (additional to the core threads count) wait until terminating.
   */
  protected Integer timeout = DEFAULT_TIMEOUT;

  protected Object readResolve() {
    if (threadKeepAliveUnit == null) {
      threadKeepAliveUnit = DEFAULT_KEEP_ALIVE_UNIT;
    }
    if (timeoutUnit == null) {
      timeoutUnit = DEFAULT_TIMEOUT_UNIT;
    }

    if (maxThreads == null) {
      maxThreads = DEFAULT_MAX_THREADS;
    }
    if (coreThreads == null) {
      coreThreads = DEFAULT_CORE_THREADS;
    }
    if (threadKeepAlive == null) {
      threadKeepAlive = DEFAULT_KEEP_ALIVE;
    }
    if (timeout == null) {
      timeout = DEFAULT_TIMEOUT;
    }

    return this;
  }

  /**
   * @return the threadKeepAliveUnit
   */
  public TimeUnit getThreadKeepAliveUnit() {
    return threadKeepAliveUnit;
  }

  /**
   * @param threadKeepAliveUnit
   *        the threadKeepAliveUnit to set
   */
  public void setThreadKeepAliveUnit(TimeUnit threadKeepAliveUnit) {
    this.threadKeepAliveUnit = threadKeepAliveUnit;
  }

  /**
   * @return the threadKeepAlive
   */
  public long getThreadKeepAlive() {
    return threadKeepAlive;
  }

  /**
   * @param threadKeepAlive
   *        the threadKeepAlive to set
   */
  public void setThreadKeepAlive(int threadKeepAlive) {
    this.threadKeepAlive = threadKeepAlive;
  }

  /**
   * @return the coreThreads
   */
  public int getCoreThreads() {
    return coreThreads;
  }

  /**
   * @param coreThreads
   *        the coreThreads to set
   */
  public void setCoreThreads(int coreThreads) {
    this.coreThreads = coreThreads;
  }

  /**
   * @return the maxThreads
   */
  public int getMaxThreads() {
    return maxThreads;
  }

  /**
   * @param maxThreads
   *        the maxThreads to set
   */
  public void setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
  }

  /**
   * @return the timeoutUnit
   */
  public TimeUnit getTimeoutUnit() {
    return timeoutUnit;
  }

  /**
   * @return the timeout
   */
  public long getTimeout() {
    return timeout;
  }

}
