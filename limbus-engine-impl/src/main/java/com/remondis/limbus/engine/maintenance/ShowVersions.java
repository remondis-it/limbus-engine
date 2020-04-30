package com.remondis.limbus.engine.maintenance;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
public class ShowVersions extends AbstractLimbusItem {

  public ShowVersions() {
    super("Versions");
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
        "The following table shows the modules included in the Limbus Engine host classpath. The information in the table are extracted from the modules Jar-Manifest. Some of the information are optional an maybe unavailable."));
    main.addComponent(new EmptySpace());
    main.addComponent(new Label(
        "Note: Only modules with a title are shown. There may be more modules (JARs) in the host's classpath that do not provide a manifest or a module title."));
    main.addComponent(new EmptySpace());

    Table<String> versions = new Table<String>("Module", "Version", "Vendor", "Build-JDK");
    TableModel<String> tableModel = versions.getTableModel();

    try {

      List<ModuleRecord> modules = getModules(tableModel);
      for (ModuleRecord r : modules) {
        tableModel.addRow(r.module, r.version, r.vendor, r.jdk);
      }
    } catch (Exception e) {
      console.showExceptionPanel(e);
    }
    versions.addTo(main);
    return main;

  }

  private List<ModuleRecord> getModules(TableModel<String> tableModel) throws IOException {
    List<ModuleRecord> modules = new LinkedList<>();

    Enumeration<URL> resources = getClass().getClassLoader()
        .getResources("META-INF/MANIFEST.MF");
    while (resources.hasMoreElements()) {
      URL nextElement = resources.nextElement();
      try (InputStream input = nextElement.openStream()) {
        Manifest manifest = new Manifest(input);
        Attributes attr = manifest.getMainAttributes();
        if (attr.getValue("Implementation-Title") != null) {
          ModuleRecord record = new ModuleRecord(manifest);
          modules.add(record);
        }
      } catch (IOException e) {
        e.printStackTrace();
        tableModel.addRow(e.getMessage(), "", "", "");
      }
    }

    Collections.sort(modules, new ModuleComparator());
    return modules;
  }

}

class ModuleRecord {
  private static final String NA = "N/A";

  String module;
  String version;
  String vendor;
  String jdk;

  public ModuleRecord(Manifest manifest) {
    Attributes attr = manifest.getMainAttributes();
    module = stringOrNA(attr.getValue("Implementation-Title"));
    version = stringOrNA(attr.getValue("Implementation-Version"));
    vendor = stringOrNA(attr.getValue("Implementation-Vendor"));
    jdk = stringOrNA(attr.getValue("Build-Jdk"));
  }

  private String stringOrNA(Object object) {
    if (object == null) {
      return NA;
    } else {
      return (String) object;
    }
  }
}

class ModuleComparator implements Comparator<ModuleRecord> {

  @Override
  public int compare(ModuleRecord o1, ModuleRecord o2) {
    return o1.module.compareTo(o2.module);
  }

}
