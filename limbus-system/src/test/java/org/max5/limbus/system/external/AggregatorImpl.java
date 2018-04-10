package org.max5.limbus.system.external;

import org.max5.limbus.Initializable;
import org.max5.limbus.system.LimbusComponent;
import org.max5.limbus.system.LimbusContainer;
import org.max5.limbus.system.LimbusSystem;

/**
 * Service definition aggregator
 *
 * @author schuettec
 *
 */
/**
 * @author schuettec
 *
 */
public class AggregatorImpl extends Initializable<RuntimeException> implements Aggregator {

  @LimbusContainer
  protected LimbusSystem system;

  @LimbusComponent
  protected Consumer consumer;

  @Override
  public void doSomething() {
    System.out.println(getMessage());
    System.out.println(toString());
  }

  @Override
  public String getMessage() {
    return consumer.consumeMessage();
  }

  /**
   * @return the system
   */
  public LimbusSystem getSystem() {
    return system;
  }

  @Override
  protected void performInitialize() throws RuntimeException {
  }

  @Override
  protected void performFinish() {
  }

  @Override
  public String toString() {
    return "[ Aggregator -> " + consumer + " ]";
  }

}
