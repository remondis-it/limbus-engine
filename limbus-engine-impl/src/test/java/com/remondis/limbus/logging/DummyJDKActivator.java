package com.remondis.limbus.logging;

import com.remondis.limbus.Initializable;

public class DummyJDKActivator extends Initializable<LoggingActivatorException> implements LoggingActivator {

  @Override
  protected void performInitialize() throws LoggingActivatorException {
  }

  @Override
  protected void performFinish() {
  }

}
