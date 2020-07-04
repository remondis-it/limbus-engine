package com.remondis.limbus.maintenance.console;

import com.googlecode.lanterna.gui2.Container;

/**
 * An item represents a leaf in the navigation tree. Selecting an {@link Action}
 * results in opening a new panel.
 *
 * 
 *
 */
public abstract class Action extends Item {
  public Action(String title) {
    super(title);
  }

  /**
   * Called by the framework when the user selects this {@link Action}. The
   * provided panel will be opened.
   *
   * @param gui The window based text gui can be used to display message dialogs.
   *
   * @return The panel to open.
   */
  public abstract Container getComponent(LimbusMaintenanceConsole gui);
}
