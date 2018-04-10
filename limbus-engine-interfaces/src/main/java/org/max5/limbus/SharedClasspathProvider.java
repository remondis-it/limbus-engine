package org.max5.limbus;

import org.max5.limbus.exceptions.LimbusClasspathException;

/**
 * This interface defines a provider that is able to construct a {@link Classpath} used by the {@link LimbusEngine} as
 * shared classpath. The use of this interface is intended for testing purposes.
 *
 * @author schuettec
 *
 */
public interface SharedClasspathProvider extends IInitializable<Exception> {

  /**
   * Called by the {@link LimbusEngine} to check the availability of the shared classpath.
   *
   * @throws LimbusClasspathException
   *         Thrown by the implementation if the shared classpath is unavailable.
   */
  void checkClasspath() throws LimbusClasspathException;

  /**
   * Called by {@link LimbusEngine} to get the shared classpath.
   *
   * @return Returns the shared classpath.
   */
  Classpath getSharedClasspath();

}
