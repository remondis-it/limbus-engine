package com.remondis.limbus.engine.actions;

/**
 * This class holds the result of an action performed by an {@link ActionService}.
 *
 * @param <R>
 *        The type of the value returned by this command.
 * 
 *
 */
public final class ActionResult<R> {

  private ActionStatus status;
  private R returnValue;

  public ActionResult(ActionStatus status, R returnValue) {
    super();
    this.status = status;
    this.returnValue = returnValue;
  }

  public ActionStatus getStatus() {
    return status;
  }

  public boolean hasValue() {
    return returnValue != null;
  }

  public R getReturnValue() {
    return returnValue;
  }

}
