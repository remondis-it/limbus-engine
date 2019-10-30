package com.remondis.limbus.maintenance;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.engine.LimbusMaintenanceConsole;

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
