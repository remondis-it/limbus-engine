package org.max5.limbus.tasks;

public interface Task {

  /**
   * Called by the {@link TaskScheduler} to execute the task.
   *
   * @throws Exception
   *         Thrown on any error.
   */
  public void execute() throws Exception;

}
