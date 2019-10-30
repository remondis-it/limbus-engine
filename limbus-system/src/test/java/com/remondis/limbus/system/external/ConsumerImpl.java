package com.remondis.limbus.system.external;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.system.LimbusComponent;
import com.remondis.limbus.utils.Lang;

public class ConsumerImpl extends Initializable<RuntimeException> implements Consumer {

  @LimbusComponent(Producer.class)
  protected Producer producer;

  @LimbusComponent(Filter.class)
  protected Filter filter;

  @Override
  public String consumeMessage() {
    String message = producer.getMessage();
    message = filter.filter(message);
    return message;
  }

  @Override
  protected void performInitialize() throws RuntimeException {
    Lang.denyNull("producer", producer);
    Lang.denyNull("filter", filter);
  }

  @Override
  protected void performFinish() {
  }

  @Override
  public String toString() {
    return "[ Consumer -> " + producer + ", " + filter + " ]";
  }

}
