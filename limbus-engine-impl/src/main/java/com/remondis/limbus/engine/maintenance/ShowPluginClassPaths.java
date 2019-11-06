package com.remondis.limbus.engine.maintenance;

import java.net.URL;
import java.util.Set;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.TextBox.Style;
import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.system.LimbusComponent;

public class ShowPluginClassPaths extends AbstractLimbusItem {

  @LimbusComponent
  protected LimbusEngine container;

  public ShowPluginClassPaths() {
    super("Plugin Classpaths");
  }

  @Override
  public Container getComponent(final LimbusMaintenanceConsole console) {
    Panel panel = new Panel(new BorderLayout());

    Label headLine = new Label(
        "The following list shows the deployed classpaths available in this container. Select a classpath entry to show the list of loaded URLs.");

    final TextBox loadedURLs = new TextBox("", Style.MULTI_LINE);
    loadedURLs.setReadOnly(true);

    ActionListBox selectClasspath = new ActionListBox();
    Set<Classpath> pluginClasspaths = container.getPluginClasspaths();
    int count = 1;
    for (Classpath pluginClasspath : pluginClasspaths) {
      final Classpath classpath = pluginClasspath;
      selectClasspath.addItem("Classpath " + count, new Runnable() {
        @Override
        public void run() {
          loadedURLs.setText("");
          for (URL url : classpath.getClasspath()) {
            loadedURLs.addLine("- " + url.toString());
          }
        }
      });
      count++;
    }

    panel.addComponent(headLine, BorderLayout.Location.TOP);
    panel.addComponent(selectClasspath.withBorder(Borders.singleLine("Deployed classpaths")),
        BorderLayout.Location.LEFT);
    panel.addComponent(loadedURLs.withBorder(Borders.singleLine("Classpath URL entries")),
        BorderLayout.Location.CENTER);
    return panel;
  }

}
