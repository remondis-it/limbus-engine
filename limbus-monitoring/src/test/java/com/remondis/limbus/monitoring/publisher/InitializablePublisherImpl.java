package com.remondis.limbus.monitoring.publisher;

import com.remondis.limbus.Initializable;

public class InitializablePublisherImpl extends Initializable<Exception> implements InitializablePublisher {
  public int init = 0;
  public int finish = 0;

  @Override
  public void reset() {
    this.init = 0;
    this.finish = 0;
  }

  @Override
  protected void performInitialize() throws Exception {
    init++;
  }

  @Override
  protected void performFinish() {
    finish++;
  }

}
