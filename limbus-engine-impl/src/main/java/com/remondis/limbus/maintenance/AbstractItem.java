package com.remondis.limbus.maintenance;

import com.remondis.limbus.IInitializable;
import com.remondis.limbus.LimbusMaintenanceConsole;

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
