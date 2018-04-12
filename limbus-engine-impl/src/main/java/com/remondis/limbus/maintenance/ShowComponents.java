package com.remondis.limbus.maintenance;

import java.util.List;

import com.remondis.limbus.LimbusMaintenanceConsole;
import com.remondis.limbus.system.Component;
import com.remondis.limbus.system.ComponentConfiguration;
import com.remondis.limbus.system.InfoRecord;
import com.remondis.limbus.system.LimbusContainer;
import com.remondis.limbus.system.LimbusSystem;

import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

/**
 * This maintenance page shows information of the modules included in the running Limbus Engine. The classpath is
 * analyzed using the classloader that loaded this class, because it is assumed that this class is part of the host
 * system.
 *
 * @author schuettec
 *
 */
public class ShowComponents extends AbstractLimbusItem {

  @LimbusContainer
  private LimbusSystem system;

  public ShowComponents() {
    super("Components");
  }

  /*
   * (non-Javadoc)
   *
   * @see com.remondis.limbus.maintenance.Action#getComponent(com.remondis.limbus.LimbusMaintenanceConsole)
   */
  @Override
  public Container getComponent(final LimbusMaintenanceConsole console) {
    Panel main = new Panel(new LinearLayout(Direction.VERTICAL));

    main.addComponent(new Label(
        "The following table shows the initialization state of the current Limbus System. The items in the table are in the (de)initialization order that occurred at runtime."));
    main.addComponent(new EmptySpace());

    Table<String> versions = new Table<String>("Status", "Required?", "Visibility", "Type");
    TableModel<String> tableModel = versions.getTableModel();

    List<InfoRecord> infoRecords = system.getInfoRecords();
    for (InfoRecord r : infoRecords) {
      String status = r.getStatus()
          .name();
      Component component = r.getComponent();
      ComponentConfiguration configuration = component.getConfiguration();
      String required = configuration.isFailOnError() ? "yes" : "no";
      String visibility = configuration.isPublicComponent() ? "public" : "private";
      String type = "";
      if (configuration.isPublicComponent()) {
        type = "Request type: " + configuration.getRequestType()
            .getName();
        type += "\n";
      }
      type += "  Impl. type: " + configuration.getComponentType()
          .getName();
      tableModel.addRow(status, required, visibility, type);
    }

    versions.addTo(main);
    return main;

  }

}
