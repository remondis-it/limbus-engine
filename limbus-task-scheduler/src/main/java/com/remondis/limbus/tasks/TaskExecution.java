package com.remondis.limbus.tasks;

import static com.remondis.limbus.tasks.TaskSchedulerImpl.*;

import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

import com.remondis.limbus.utils.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TaskExecution implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(TaskExecution.class);

  private TaskSchedulerImpl scheduler;
  private Task task;
  private Function<Boolean, Long> scheduleRateFunction;
  private long currentRate;
  private boolean lastSuccess;
  private ScheduledFuture<?> future;

  private boolean rejected;

  TaskExecution(TaskSchedulerImpl scheduler, Task task, Function<Boolean, Long> scheduleRateFunction) {
    super();
    this.task = task;
    this.scheduleRateFunction = scheduleRateFunction;
    this.scheduler = scheduler;
  }

  @Override
  public void run() {
    StopWatch s = new StopWatch();
    this.rejected = false;
    boolean success = false;
    try {
      s.start();
      task.execute();
      success = true;
    } catch (Exception e) {
      success = false;
      log.warn(String.format("The periodic task %s failed with an exception.", task.getClass()
          .getName()), e);
    } finally {
      s.stop();
    }
    lastSuccess = success;
    rescheduleOnDemand(success, s.getMillisecondsRuntime());
  }

  private void logException(long newRate, boolean success) {
    if (success) {
      log.warn("The priodic task {} was successful after failing - re-scheduling this task with rate {}ms.",
          task.getClass()
              .getName(),
          newRate);
    } else {
      log.warn(String.format("The priodic task %s was unsuccessful - re-scheduling this task with rate %dms.",
          task.getClass()
              .getName(),
          newRate));
    }
  }

  private void rescheduleOnDemand(boolean success, long lastRuntime) {
    if (lastRuntime >= currentRate) {
      // buschmann - 23.05.2017 : The scheduled executor service automatically prevents overlapping of task executions.
      // So subsequent executions will be delayed until after the previous one has finished. We only need to warn here.
      log.warn(String.format("Runtime of task %s was bigger then reschedule delay.", this.getTaskInfo()
          .getTaskName()));
    }

    long newRate = normalizeRate(scheduleRateFunction.apply(success));
    if (currentRate != newRate) {
      logException(newRate, success);
      // Cancel this execution
      cancel();
      currentRate = newRate;
      scheduler.reschedule(this, newRate);
    }

  }

  public void setCurrentRate(Long currentRate) {
    this.currentRate = currentRate;
  }

  public void setFuture(ScheduledFuture<?> future) {
    this.future = future;
  }

  public void cancel() {
    if (future != null) {
      future.cancel(false);
      try {
        future.get();
      } catch (Exception e) {
        /*
         * Ignore any exception because:
         * CancellationException: Is a result of cancelling the job, but this is our intention here.
         *
         * ExecutionException: if the computation threw an exception. This exception cannot be handled here. Exceptions
         * are handled inside the run method.
         *
         * InterruptedException - if the current thread was interrupted while waiting, we cannot do anything.
         *
         */
      }
    }
  }

  public TaskInfo getTaskInfo() {
    return new TaskInfo(task.getClass()
        .getName(), lastSuccess, currentRate, rejected);
  }

  public void wasRejected() {
    this.rejected = true;
  }

}
