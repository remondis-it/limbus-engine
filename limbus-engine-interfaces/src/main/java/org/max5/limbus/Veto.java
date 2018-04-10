package org.max5.limbus;

/**
 * This interface defines an object that can be used to signal a veto against an operation. For example, this interface
 * is used by {@link DeploymentListener} to give subscribers a chance to reject an undeploy attempt.
 *
 * @author schuettec
 *
 */
public interface Veto {

  /**
   * Called by subscribers to reject the attempt to perform a specified operation.
   */
  public void veto();

}
