package com.remondis.limbus;

/**
 * This interface defines classes that enumerate a set of default components. Those components are assumed to be the
 * minimal set of system components that are required to get a valid runnable system. The Limbus Engine provides an
 * implementation of this class to specify the minimal set of components that can be used with Limbus Staging to get a
 * running system that is the minimal configuration.
 *
 * @author schuettec
 *
 */
public interface DefaultComponents {

  public void applyDefaultComponents(DefaultComponentsConsumer defaultComponentsConsumer);
}
