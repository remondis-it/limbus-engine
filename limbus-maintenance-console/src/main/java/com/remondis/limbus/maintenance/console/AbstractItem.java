package com.remondis.limbus.maintenance.console;

import com.remondis.limbus.api.IInitializable;

/**
 * This class defines an abstract item that implements basic methods to be used
 * in {@link LimbusMaintenanceConsole}.
 *
 * 
 *
 */
public abstract class AbstractItem extends Action implements IInitializable<Exception> {

  public AbstractItem(String title) {
    super(title);
  }

  @Override
  public void initialize() throws Exception {
  }

  @Override
  public void finish() {
  }
}
