package com.remondis.limbus.tasks;

import java.util.List;
import java.util.function.Function;

import com.remondis.limbus.api.IInitializable;

/**
 * This is the global task scheduler that provides fixed rate scheduling for periodic tasks.
 *
 * @author schuettec
 *
 */
public interface TaskScheduler extends IInitializable<Exception> {

  /**
   * Removes a scheduled {@link Task} from the periodic task scheduler..
   *
   * @param task
   *        The task to schedule
   */
  public void unschedulePeriodicTask(Task task);

  /**
   * Schedules a {@link Task} for periodic execution using an adaptive frequency.
   *
   * @param task
   *        The task to schedule
   * @param scheduleRateFunction
   *        The function that calculates the task execution frequency in milliseconds based on the last
   *        success status. <b>Note: The minimum frequency is 500ms!</b>
   */
  public void schedulePeriodicTask(Task task, Function<Boolean, Long> scheduleRateFunction);

  /**
   * @return Returns the current snapshot of the tasks scheduled.
   */
  public List<TaskInfo> getSchedulerInfo();
}
