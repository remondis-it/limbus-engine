package com.remondis.limbus.system;

/**
 * This interface describes a component consumer that performs operations on a component managed by the
 * {@link LimbusSystem}.
 *
 * @author schuettec
 *
 */
@FunctionalInterface
interface ComponentConsumer {

  /**
   * Processes a component.
   *
   * @param component
   *        The component to perform an operation on.
   * @throws LimbusComponentException
   *         Thrown on any component specific error.
   */
  void consume(Component component) throws LimbusComponentException;

}
