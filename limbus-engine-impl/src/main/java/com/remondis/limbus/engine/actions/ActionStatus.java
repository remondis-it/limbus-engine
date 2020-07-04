package com.remondis.limbus.engine.actions;

/**
 * Enumeration for action states.
 *
 * 
 *
 */
public enum ActionStatus {

  /**
   * Status for new actions.
   */
  OPEN,
  /**
   * Action detected and executing.
   */
  IN_WORK,
  /**
   * Failed to execute action.
   */
  FAILURE,
  /**
   * Action could not be executed. This can for example be caused by database lock. Marked to re-schedule.
   */
  RESCHEDULE,
  /**
   * Action finished successfully.
   */
  FINISHED,

  /**
   * Action was denied to be executed on the container.
   */
  DENIED;

  public static ActionStatus getValue(int statusPos) {
    ActionStatus returnValue = ActionStatus.values()[statusPos];
    return returnValue;
  }
}
