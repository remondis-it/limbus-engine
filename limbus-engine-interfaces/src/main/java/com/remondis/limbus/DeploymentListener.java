package com.remondis.limbus;

/**
 * Listener interface to receive deployment events.
 *
 * @author schuettec
 *
 */
public interface DeploymentListener {

  /**
   * Called by the {@link LimbusEngine} if a new {@link Classpath} was deployed on the current instance of
   * {@link LimbusEngine}.
   *
   * @param classpath
   *        The deployed classpath.
   */
  public default void classpathDeployed(Classpath classpath) {

  }

  /**
   * Called by the {@link LimbusEngine} to signal that the specified {@link Classpath} will be undeployed.
   *
   * @param classpath
   *        The classpath that is about to be undeployed.
   * @param veto
   *        Used by callees to veto against this undeploy operation.
   */
  public default void classpathUndeploying(Classpath classpath, Veto veto) {

  }

  /**
   * Called by the {@link LimbusEngine} to signal that the specified {@link Classpath} was undeployed.
   *
   * @param classpath
   *        The classpath that is about to be undeployed.
   */
  public default void classpathUndeployed(Classpath classpath) {

  }
}
