package com.remondis.limbus.system.external;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.system.LimbusSystem;
import com.remondis.limbus.system.api.LimbusComponent;
import com.remondis.limbus.system.api.LimbusContainer;

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
