package com.remondis.limbus.system.external;

import com.remondis.limbus.api.Initializable;

public class ProducerImpl extends Initializable<RuntimeException> implements Producer {

  public static final String MESSAGE = "Producer produced a message.";

  @Override
  public String getMessage() {
    return MESSAGE;
  }

  @Override
  protected void performInitialize() throws RuntimeException {
  }

  @Override
  protected void performFinish() {
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[ Producer ]";
  }

}
