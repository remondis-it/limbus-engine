package org.max5.limbus.logging;

import org.max5.limbus.Initializable;

public class DummyJDKActivator extends Initializable<LoggingActivatorException> implements LoggingActivator {

  @Override
  protected void performInitialize() throws LoggingActivatorException {
  }

  @Override
  protected void performFinish() {
  }

}
