package com.remondis.limbus.engine.logging;

import com.remondis.limbus.activators.logging.LoggingActivator;
import com.remondis.limbus.activators.logging.LoggingActivatorException;
import com.remondis.limbus.api.Initializable;

public class DummyJDKActivator extends Initializable<LoggingActivatorException> implements LoggingActivator {

  @Override
  protected void performInitialize() throws LoggingActivatorException {
  }

  @Override
  protected void performFinish() {
  }

}
