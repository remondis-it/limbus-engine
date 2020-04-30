package com.remondis.limbus.engine.maintenance;

import com.remondis.limbus.api.IInitializable;

/**
 * This class defines an abstract item that implements basic methods to be used in {@link LimbusMaintenanceConsole}.
 *
 * @author schuettec
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
