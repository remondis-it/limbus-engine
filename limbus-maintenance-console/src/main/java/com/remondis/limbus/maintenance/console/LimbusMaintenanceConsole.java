package com.remondis.limbus.maintenance.console;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.remondis.limbus.api.IInitializable;

/**
 * This is the public interface for the Limbus maintenance console.
 *
 * @author schuettec
 *
 */
public interface LimbusMaintenanceConsole extends IInitializable<Exception> {

  /**
   * Shows the specified exception using the message panel.
   * 
   * @param e The exception to show.
   */
  public void showExceptionPanel(Exception e);

  /**
   * Shows the specified string using the message panel
   *
   * @param message The string to show.
   */
  public void showMessagePanel(String message);

  /**
   * Add an item tree to the root category of the maintenance console.
   *
   * @param items The item tree to add.
   */
  public void addNavigationItems(Item... items);

  /**
   * @return Returns the reference to the current window GUI.
   */
  public MultiWindowTextGUI getGui();

  /**
   * Updates the screen.
   */
  public void updateScreen();

  /**
   * Updates the current page.
   */
  public void updateCurrentPage();
}
