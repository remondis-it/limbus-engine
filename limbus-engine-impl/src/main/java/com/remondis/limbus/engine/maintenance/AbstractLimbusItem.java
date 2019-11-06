package com.remondis.limbus.engine.maintenance;

import com.remondis.limbus.system.LimbusComponent;

/**
 * This class defines an abstract item that implements basic methods to be used in {@link LimbusMaintenanceConsole} and
 * <b>add the item to the maintenance console root category</b>. All basic Limbus Maintenance features are added into
 * the root category..
 *
 * @author schuettec
 *
 */
public abstract class AbstractLimbusItem extends AbstractItem {

  @LimbusComponent
  protected LimbusMaintenanceConsole console;

  public AbstractLimbusItem(String title) {
    super(title);
  }

  @Override
  public void initialize() throws Exception {
    super.initialize();
    console.addNavigationItems(this);
  }

}
