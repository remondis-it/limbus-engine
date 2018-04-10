package org.max5.limbus.tasks;

/**
 * Represents the information snapshot of a scheduled task, managed by {@link TaskScheduler}.
 *
 * @author schuettec
 *
 */
public class TaskInfo {

  private String taskName;
  private boolean lastSuccess;
  private long currentWaitTime;
  private boolean rejected;

  public TaskInfo(String taskName, boolean lastSuccess, long currentWaitTime, boolean rejected) {
    this.taskName = taskName;
    this.lastSuccess = lastSuccess;
    this.currentWaitTime = currentWaitTime;
    this.rejected = rejected;
  }

  public String getTaskName() {
    return taskName;
  }

  public boolean isLastSuccess() {
    return lastSuccess;
  }

  public long getCurrentWaitTime() {
    return currentWaitTime;
  }

  public boolean isRejected() {
    return rejected;
  }

}
