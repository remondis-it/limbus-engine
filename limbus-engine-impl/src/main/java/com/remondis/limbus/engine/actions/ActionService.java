package com.remondis.limbus.engine.actions;

import com.remondis.limbus.api.IInitializable;

/**
 * This is the definition of the {@link ActionService}.
 *
 * 
 *
 */
public interface ActionService extends IInitializable<Exception> {

  /**
   * Executes the specified method of a class on the underlying Limbus container synchronously.
   *
   * @param action
   *        The container with the action to execute.
   *
   * @return Returns the {@link ActionResult} as the result of the executed action.
   * @throws ActionException
   *         Thrown on any error.
   */
  public ActionResult<?> executeAction(ActionExecution action) throws ActionException;

}
