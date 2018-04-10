package org.max5.limbus.maintenance;

import org.max5.limbus.IInitializable;
import org.max5.limbus.LimbusMaintenanceConsole;

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
