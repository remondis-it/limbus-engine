package com.remondis.limbus.engine.maintenance;

import java.io.File;
import java.util.concurrent.Callable;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutData;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.remondis.limbus.engine.aether.AetherUtil;
import com.remondis.limbus.engine.api.DeployService;
import com.remondis.limbus.launcher.EngineLauncher;
import com.remondis.limbus.system.api.LimbusComponent;
import com.remondis.limbus.utils.Files;

public class ShowEnvironment extends AbstractLimbusItem {

  @LimbusComponent
  protected DeployService deployService;

  public ShowEnvironment() {
    super("Environment");
  }

  @Override
  public Container getComponent(final LimbusMaintenanceConsole console) {
    Panel main = new Panel(new LinearLayout(Direction.VERTICAL));

    Panel environment = new Panel(new GridLayout(3));
    addGridHeader(environment);
    addFileInfo("Deploy directory:", deployService.getDeployDirectoryUnchecked(), environment);
    addFileInfo("Work directory:", deployService.getWorkDirectoryUnchecked(), environment);

    Panel deploy = new Panel(new GridLayout(3));
    addInfo("Hot deploy listener active:", toYesNo(deployService.isHotDeployFolderActive()), "", deploy);
    addInfo("Cleaning work directory:", toYesNo(deployService.isCleanWorkDirectory()), "", deploy);

    Panel maven = new Panel(new GridLayout(3));
    addGridHeader(maven);
    addDirectoryInfo("User Home:", new Callable<File>() {

      @Override
      public File call() throws Exception {
        return AetherUtil.getUserHome();
      }
    }, maven);
    addDirectoryInfo("Maven User Home:", new Callable<File>() {

      @Override
      public File call() throws Exception {
        return AetherUtil.getUserMavenConfigurationHome();
      }
    }, maven);
    addDirectoryInfo("Maven User Repository:", new Callable<File>() {

      @Override
      public File call() throws Exception {
        return AetherUtil.getUserLocalRepository();
      }
    }, maven);
    addDirectoryInfo("Maven User Settings:", new Callable<File>() {

      @Override
      public File call() throws Exception {
        return AetherUtil.getUserSettingsFile();
      }
    }, maven);

    Panel control = new Panel();
    ActionListBox actions = new ActionListBox();
    actions.addItem("Shutdown Engine", new Runnable() {

      @Override
      public void run() {
        MultiWindowTextGUI gui = console.getGui();
        MessageDialogButton selectedButton = new MessageDialogBuilder().setTitle("Engine shutdown")
            .setText("Are you sure to stop all plugins and shutdown the engine?")
            .addButton(MessageDialogButton.Yes)
            .addButton(MessageDialogButton.No)
            .build()
            .showDialog(gui);
        if (selectedButton == MessageDialogButton.Yes) {
          EngineLauncher.shutdownEngine();
        }
      }
    });
    actions.addItem("Trigger garbage collection", new Runnable() {
      @Override
      public void run() {
        System.gc();
      }
    });
    control.addComponent(actions);

    main.addComponent(environment.withBorder(Borders.singleLine("Environment Paths")), fillLinear());
    main.addComponent(deploy.withBorder(Borders.singleLine("Deploy Paths")), fillLinear());
    main.addComponent(maven.withBorder(Borders.singleLine("Maven Paths")), fillLinear());
    main.addComponent(control.withBorder(Borders.singleLine("Engine Control Actions")), fillLinear());

    return main;
  }

  private LayoutData fillLinear() {
    return LinearLayout.createLayoutData(com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill);
  }

  private void addGridHeader(Panel panel) {
    panel.addComponent(new Label("Directory"), getLabelLayoutData());
    panel.addComponent(new Label("Filesystem path"), getValueLayoutData());
    panel.addComponent(new Label("Accessible"), getDetailLayoutData());
  }

  private void addDirectoryInfo(String label, Callable<File> directoryProvider, Panel panel) {
    try {
      File directory = directoryProvider.call();
      addFileInfo(label, directory, panel);
    } catch (Exception e) {
      addInfo(label, String.format("Error: %s", e.toString()), "no", panel);
    }
  }

  private void addFileInfo(String label, File directory, Panel panel) {
    String absolutePath = directory.getAbsolutePath();
    boolean accessible = false;
    if (directory.isDirectory()) {
      accessible = Files.isAccessibleDirectory(directory);
    } else {
      accessible = Files.isAccessibleFile(directory);
    }
    addInfo(label, absolutePath, toYesNo(accessible), panel);
  }

  private String toYesNo(boolean accessible) {
    return accessible ? "yes" : "no";
  }

  private void addInfo(String label, String value, String detail, Panel panel) {
    panel.addComponent(new Label(label), getLabelLayoutData());
    panel.addComponent(new Label(value), getValueLayoutData());
    panel.addComponent(new Label(detail), getDetailLayoutData());
  }

  private LayoutData getLabelLayoutData() {
    return GridLayout.createLayoutData(Alignment.END, Alignment.CENTER);
  }

  private LayoutData getValueLayoutData() {
    return GridLayout.createLayoutData(Alignment.FILL, Alignment.CENTER);
  }

  private LayoutData getDetailLayoutData() {
    return GridLayout.createLayoutData(Alignment.CENTER, Alignment.CENTER);
  }

}
