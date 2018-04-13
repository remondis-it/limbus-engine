package com.remondis.limbus.tasks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.Initializable;
import com.remondis.limbus.LimbusProperties;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.system.LimbusComponent;
import com.remondis.limbus.utils.Lang;

public class TaskSchedulerImpl extends Initializable<Exception> implements TaskScheduler, ThreadFactory {

  private static final String PERIODIC_TASKS_THREAD_POOL_SIZE = "periodic-tasks.thread-pool.size";
  private static final String SHUTDOWN_TIMEOUT = "periodic-tasks.shutdown-timeout.value";
  private static final String SHUTDOWN_TIMEOUT_UNIT = "periodic-tasks.shutdown-timeout.timeUnit";

  private static final Logger log = LoggerFactory.getLogger(TaskSchedulerImpl.class);

  private static final AtomicInteger threadCount = new AtomicInteger();

  @LimbusComponent
  protected LimbusFileService filesystem;

  protected long shutdownTimeout;

  protected TimeUnit shutdownTimeoutUnit;

  protected ScheduledExecutorService scheduler;

  protected ConcurrentHashMap<Task, TaskExecution> tasks;

  @Override
  protected void performInitialize() throws Exception {
    LimbusProperties properties = new LimbusProperties(filesystem, TaskSchedulerImpl.class, true, false);

    int threadCount = properties.getInt(PERIODIC_TASKS_THREAD_POOL_SIZE);
    this.shutdownTimeout = properties.getLong(SHUTDOWN_TIMEOUT);
    this.shutdownTimeoutUnit = properties.getEnum(SHUTDOWN_TIMEOUT_UNIT, TimeUnit.class);

    // extra scheduler for cyclical call of commands so that the scheduler can be stopped separately
    scheduler = Executors.newScheduledThreadPool(threadCount, this);

    this.tasks = new ConcurrentHashMap<Task, TaskExecution>();
  }

  @Override
  protected void performFinish() {
    // Cancel all tasks
    if (tasks != null) {
      Iterator<TaskExecution> it = tasks.values()
          .iterator();
      while (it.hasNext()) {
        TaskExecution exec = it.next();
        exec.cancel();
      }
      tasks.clear();
    }

    if (scheduler != null) {
      log.info("Shutting down task scheduler.");
      scheduler.shutdown();
      try {
        scheduler.awaitTermination(shutdownTimeout, shutdownTimeoutUnit);
        scheduler.shutdownNow();
      } catch (InterruptedException e) {
        log.info("Shutting down task scheduler failed.", e);
      }
    }

  }

  @Override
  public void schedulePeriodicTask(Task task, Function<Boolean, Long> scheduleRateFunction) {
    Lang.denyNull("task", task);
    Lang.denyNull("scheduleRateFunction", scheduleRateFunction);

    checkState();
    TaskExecution execution = new TaskExecution(this, task, scheduleRateFunction);
    Long initialRate = getInitialRate(scheduleRateFunction);
    execution.setCurrentRate(initialRate);
    ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(execution, 0, initialRate, TimeUnit.MILLISECONDS);
    execution.setFuture(future);
    tasks.put(task, execution);
  }

  @Override
  public void unschedulePeriodicTask(Task task) {
    Lang.denyNull("task", task);

    if (tasks.containsKey(task)) {
      TaskExecution taskExecution = tasks.get(task);
      tasks.remove(task);
      taskExecution.cancel();
    } else {
      throw new NoSuchElementException("Attempt to unschedule an unknown task.");
    }
  }

  public void reschedule(TaskExecution execution, Long newRate) {
    try {
      ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(execution, newRate, normalizeRate(newRate),
          TimeUnit.MILLISECONDS);
      execution.setFuture(future);
    } catch (RejectedExecutionException e) {
      execution.wasRejected();
    }
  }

  protected static long normalizeRate(long rate) {
    return Math.max(500l, rate);
  }

  private Long getInitialRate(Function<Boolean, Long> scheduleRateFunction) {
    return normalizeRate(scheduleRateFunction.apply(true));
  }

  @Override
  public List<TaskInfo> getSchedulerInfo() {
    checkState();
    List<TaskInfo> info = new LinkedList<>();
    Iterator<TaskExecution> it = tasks.values()
        .iterator();
    while (it.hasNext()) {
      TaskExecution e = it.next();
      info.add(e.getTaskInfo());
    }
    return info;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread newThread = new Thread(r, "Limbus Task Scheduler - Thread " + threadCount.incrementAndGet());
    return newThread;
  }

}
