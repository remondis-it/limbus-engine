package com.remondis.limbus.engine.maintenance;

import java.net.URL;

import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.TextBox.Style;
import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.system.LimbusComponent;

public class ShowSharedClassPath extends AbstractLimbusItem {

  @LimbusComponent
  protected LimbusEngine container;

  public ShowSharedClassPath() {
    super("Shared Classpath");
  }

  @Override
  public Container getComponent(final LimbusMaintenanceConsole console) {

    Panel panel = new Panel(new BorderLayout());
    Label headLine = new Label("The following list shows the loaded URLs loaded in the shared classpath.");
    panel.addComponent(headLine, BorderLayout.Location.TOP);

    TextBox sharedClasspath = new TextBox("", Style.MULTI_LINE);
    sharedClasspath.setReadOnly(true);

    Classpath classpath = container.getSharedClasspath();
    for (URL url : classpath.getClasspath()) {
      sharedClasspath.addLine("- " + url.toString());
    }

    panel.addComponent(sharedClasspath, BorderLayout.Location.CENTER);
    return panel;
  }

}
