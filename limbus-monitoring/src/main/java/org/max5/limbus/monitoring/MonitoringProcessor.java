package org.max5.limbus.monitoring;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the main processing unit that routes recorded publisher calls to publisher instances in a high
 * concurrent non-blocking thread-safe manner. This class uses a thread pool to process publisher calls faster. The
 * calls are routed to the registered publishers.<b>Note: The performance of publishing monitoring records is mainly
 * defined by the publisher implementations!</b>.
 *
 *
 *
 * @author schuettec
 *
 */
public class MonitoringProcessor implements ThreadFactory, RejectedExecutionHandler {

  private static final Logger log = LoggerFactory.getLogger(MonitoringProcessor.class);

  private static final String MONITORING_THREAD_NAME = "Monitoring thread ";

  private static AtomicInteger threadCount = new AtomicInteger();

  private ThreadPoolExecutor threadPool;

  private long timeout;
  private TimeUnit timeoutUnit;

  public MonitoringProcessor(MonitoringConfiguration config) {
    ProcessingConfig c = config.getProcessing();
    this.threadPool = new ThreadPoolExecutor(c.getCoreThreads(), c.getMaxThreads(), c.getThreadKeepAlive(),
        c.getThreadKeepAliveUnit(), new LinkedBlockingQueue<Runnable>(), this, this);
    this.timeout = c.getTimeout();
    this.timeoutUnit = c.getTimeoutUnit();
  }

  public void submitRecord(MethodCall call) {
    String clientName = call.getClientName();
    Class<?> publisherInterface = call.getDeclaringClass();
    Set<Object> publishers = MonitoringFactory.getPublishers(clientName, publisherInterface);
    for (Object p : publishers) {
      threadPool.submit(new PublisherTask(call, p));
    }
  }

  public void shutdown() throws SecurityException {
    this.threadPool.shutdownNow();
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = new Thread(r, MONITORING_THREAD_NAME + threadCount.incrementAndGet());
    thread.setDaemon(true);
    return thread;
  }

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    if (log.isTraceEnabled()) {
      log.trace("A monitoring record was rejected by the processing thread pool.");
    }
  }

  public void shutdown(boolean awaitTermination) {
    if (awaitTermination) {
      threadPool.shutdown();
      try {
        threadPool.awaitTermination(timeout, timeoutUnit);
      } catch (InterruptedException e) {
        log.warn("Awaiting termination was interrupted.");
      }
    } else {
      threadPool.shutdownNow();
    }
  }

}
