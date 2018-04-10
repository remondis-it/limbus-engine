package org.max5.limbus.system;

/**
 * This is a listener interface for subscribers that need a notification when the {@link LimbusSystem} instance was
 * fully initialized.
 *
 * @author schuettec
 *
 */
public interface LimbusSystemListener {

  /**
   * Called by {@link LimbusSystem} on subscribers to signal that the {@link LimbusSystem} instance was successfully
   * initialized.
   */
  public void postInitialize();

  /**
   * Called by {@link LimbusSystem} on subscribers to signal that the {@link LimbusSystem} is about to finish all
   * components.
   */
  public void preDestroy();

}
