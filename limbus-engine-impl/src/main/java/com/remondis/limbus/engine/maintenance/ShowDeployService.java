package com.remondis.limbus.engine.maintenance;

import java.util.Arrays;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutData;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.TextBox.Style;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.LimbusException;
import com.remondis.limbus.engine.api.PluginDeployService;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.engine.api.NoSuchDeploymentException;
import com.remondis.limbus.engine.api.UndeployVetoException;
import com.remondis.limbus.engine.api.security.LimbusSecurity;
import com.remondis.limbus.system.api.LimbusComponent;
import com.remondis.limbus.utils.Lang;

public class ShowDeployService extends AbstractLimbusItem {

  @LimbusComponent
  protected PluginDeployService deployService;

  @LimbusComponent
  protected LimbusEngine engine;

  @LimbusComponent
  protected LimbusSecurity limbusSecurity;

  public ShowDeployService() {
    super("Un-/Deploy classpath");
  }

  @Override
  public Container getComponent(final LimbusMaintenanceConsole console) {
    Panel main = new Panel(new LinearLayout(Direction.VERTICAL));

    createDeployFromFilesystemPanel(console, main);

    createDeployFromMavenPanel(console, main);

    Panel undeployPanel = new Panel(new LinearLayout(Direction.VERTICAL));
    {
      Label hint = new Label(
          "Select a deployed component from the list to stop the corresponding plugins and undeploy the classpath. The deployed components are displayed by their deploy folder name.");

      final ActionListBox undeployList = new ActionListBox();
      createUndeployActionList(undeployList, console);
      undeployPanel.addComponent(hint, fillLinear());
      undeployPanel.addComponent(undeployList.withBorder(Borders.singleLine("Deployed components")), fillLinear());
    }
    main.addComponent(undeployPanel.withBorder(Borders.singleLine("Undeploy")),

        fillLinear());

    return main;
  }

  private void createUndeployActionList(final ActionListBox undeployList, final LimbusMaintenanceConsole console) {
    undeployList.clearItems();
    // buschmann - 04.05.2017 : Note: Only classpaths deployed by the Deploy Service can be undeployed via maintanance
    // console. It would make sense to provide more undeployable classpaths here using
    // LimbusEngine.getPluginClasspaths().
    for (String deployName : deployService.getDeployedComponents()) {
      final String undeployName = deployName;
      undeployList.addItem(deployName, new Runnable() {

        @Override
        public void run() {
          MultiWindowTextGUI gui = console.getGui();
          try {
            MessageDialogButton selectedButton = new MessageDialogBuilder()
                .setTitle(String.format("Undeploy of \"%s\"", undeployName))
                .setText("Are you sure to stop all plugins of this deployment and undeploy this classpath?")
                .addButton(MessageDialogButton.Yes)
                .addButton(MessageDialogButton.No)
                .build()
                .showDialog(gui);
            if (selectedButton == MessageDialogButton.Yes) {
              Classpath classpath = engine.getClasspath(undeployName);
              engine.undeployPlugin(classpath);
            }
          } catch (NoSuchDeploymentException e) {
            new MessageDialogBuilder().setTitle("Error while undeploying")
                .setText("An error occurred while undeploying the classpath. Showing exception.")
                .addButton(MessageDialogButton.OK)
                .build()
                .showDialog(gui);
            console.showExceptionPanel(e);
          } catch (UndeployVetoException e) {
            new MessageDialogBuilder().setTitle("Undeploy denied!")
                .setText("The undeploy action is not allowed due to veto!")
                .addButton(MessageDialogButton.OK)
                .build()
                .showDialog(gui);
            console.showExceptionPanel(e);
          } finally {
            createUndeployActionList(undeployList, console);
          }
        }
      });
    }

  }

  private void createDeployFromMavenPanel(final LimbusMaintenanceConsole console, Panel main) {
    Panel fromMavenRepository = new Panel(new LinearLayout(Direction.VERTICAL));
    {
      Label hint = new Label(
          "A deployment can be done by specifying a Maven artifact using groupId, artifactId and version. The extension field is optional and defaults to \"jar\".");
      Panel pluginData = new Panel(new GridLayout(2));
      pluginData.addComponent(new Label("Group Id:"), GridLayout.createLayoutData(Alignment.END, Alignment.CENTER));
      final TextBox txtGroupId = createMavenCoordTextBox();
      pluginData.addComponent(txtGroupId, GridLayout.createLayoutData(Alignment.FILL, Alignment.CENTER));

      pluginData.addComponent(new Label("Artifact Id:"), GridLayout.createLayoutData(Alignment.END, Alignment.CENTER));
      final TextBox txtArtifactId = createMavenCoordTextBox();
      pluginData.addComponent(txtArtifactId, GridLayout.createLayoutData(Alignment.FILL, Alignment.CENTER));

      pluginData.addComponent(new Label("Version:"), GridLayout.createLayoutData(Alignment.END, Alignment.CENTER));
      final TextBox txtVersion = createMavenCoordTextBox();
      pluginData.addComponent(txtVersion, GridLayout.createLayoutData(Alignment.FILL, Alignment.CENTER));

      pluginData.addComponent(new Label("Extension:"), GridLayout.createLayoutData(Alignment.END, Alignment.CENTER));
      final TextBox txtExtension = createMavenCoordTextBox(PluginDeployService.DEFAULT_MAVEN_EXTENSION);
      pluginData.addComponent(txtExtension, GridLayout.createLayoutData(Alignment.FILL, Alignment.CENTER));

      pluginData.addComponent(new EmptySpace());
      pluginData.addComponent(new Button("Deploy", new Runnable() {

        @Override
        public void run() {

          MultiWindowTextGUI gui = console.getGui();
          Window waitWindow = null;
          try {
            String groupId = txtGroupId.getText();
            String artifactId = txtArtifactId.getText();
            String extension = txtExtension.getText();
            String version = txtVersion.getText();
            String deployName = deployService.toDeployName(groupId, artifactId, extension, version);

            if (Lang.isEmpty(groupId) || Lang.isEmpty(artifactId) || Lang.isEmpty(version)) {
              new MessageDialogBuilder().setTitle("Deploy from Maven Repository")
                  .setText("Enter Maven coordinates first!")
                  .addButton(MessageDialogButton.OK)
                  .build()
                  .showDialog(gui);
            } else {
              if (engine.hasClasspath(deployName)) {
                new MessageDialogBuilder().setTitle("Error while deploying")
                    .setText("A deployment with the same name already exists. Please choose another name.")
                    .addButton(MessageDialogButton.OK)
                    .build()
                    .showDialog(gui);
              } else {
                waitWindow = new BasicWindow("Please wait...");
                Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
                mainPanel.addComponent(new Label("Downloading artifact from Maven repository."));
                mainPanel.addComponent(new Label("Please wait..."));
                waitWindow.setHints(Arrays.asList(Window.Hint.MODAL));
                waitWindow.setComponent(mainPanel);
                gui.addWindow(waitWindow);
                console.updateScreen();

                // TODO - schuettec - 14.10.2016 : Only sandbox default permissions are granted for plugins deployed by
                // the maintenance console.
                deployService.deployMavenArtifact(groupId, artifactId, extension, version,
                    limbusSecurity.getSandboxDefaultPermissions());
              }
            }
          } catch (LimbusException e) {
            new MessageDialogBuilder().setTitle("Error while deploying")
                .setText("An error occurred while deploying the classpath. Showing exception.")
                .addButton(MessageDialogButton.OK)
                .build()
                .showDialog(gui);
            console.showExceptionPanel(e);
          } finally {
            if (waitWindow != null) {
              gui.removeWindow(waitWindow);
            }
          }
        }
      }));

      fromMavenRepository.addComponent(hint, fillLinear());
      fromMavenRepository.addComponent(new EmptySpace(new TerminalSize(0, 1)));
      fromMavenRepository.addComponent(pluginData, fillLinear());
    }
    main.addComponent(fromMavenRepository.withBorder(Borders.singleLine("Deploy from Maven Repository")), fillLinear());
  }

  private TextBox createMavenCoordTextBox() {
    return createMavenCoordTextBox("");
  }

  private TextBox createMavenCoordTextBox(String initialContent) {
    return new TextBox(new TerminalSize(40, 1), initialContent, Style.SINGLE_LINE);
  }

  private void createDeployFromFilesystemPanel(final LimbusMaintenanceConsole console, Panel main) {
    Panel fromFileSystem = new Panel(new LinearLayout(Direction.VERTICAL));
    {
      Label hint = new Label(
          "Use this feature to start a deployment process for a new plugin folder placed in the work directory of the engine. Before deploying make sure that the plugin folder contains all neccessary JAR files.");
      Panel pluginData = new Panel(new GridLayout(3));
      pluginData.addComponent(new Label("Deployment folder name:"),
          GridLayout.createLayoutData(Alignment.END, Alignment.CENTER));
      final TextBox textBox = new TextBox();
      pluginData.addComponent(textBox, GridLayout.createLayoutData(Alignment.FILL, Alignment.CENTER));

      pluginData.addComponent(new Button("Deploy", new Runnable() {

        @Override
        public void run() {
          MultiWindowTextGUI gui = console.getGui();
          try {
            String deployName = textBox.getText();
            if (Lang.isEmpty(deployName)) {
              new MessageDialogBuilder().setTitle("Deploy from filesystem")
                  .setText("Enter deployment folder name first!")
                  .addButton(MessageDialogButton.OK)
                  .build()
                  .showDialog(gui);
            } else {
              if (engine.hasClasspath(deployName)) {
                new MessageDialogBuilder().setTitle("Error while deploying")
                    .setText("A deployment with the same name already exists. Please choose another name.")
                    .addButton(MessageDialogButton.OK)
                    .build()
                    .showDialog(gui);
              } else {
                // TODO - schuettec - 14.10.2016 : Only sandbox default permissions are granted for plugins deployed by
                // the maintenance console.
                deployService.deployFromFilesystem(deployName, limbusSecurity.getSandboxDefaultPermissions());
              }
            }
          } catch (LimbusException e) {
            new MessageDialogBuilder().setTitle("Error while deploying")
                .setText("An error occurred while deploying the classpath. Showing exception.")
                .addButton(MessageDialogButton.OK)
                .build()
                .showDialog(gui);
            console.showExceptionPanel(e);
          }
        }
      }));

      fromFileSystem.addComponent(hint, fillLinear());
      fromFileSystem.addComponent(new EmptySpace(new TerminalSize(0, 1)));
      fromFileSystem.addComponent(pluginData, fillLinear());
    }
    main.addComponent(fromFileSystem.withBorder(Borders.singleLine("Deploy from filesystem")), fillLinear());
  }

  private LayoutData fillLinear() {
    return LinearLayout.createLayoutData(com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill);
  }

  // private void addGridHeader(Panel panel) {
  // panel.addComponent(new Label("Directory"), getLabelLayoutData());
  // panel.addComponent(new Label("Filesystem path"), getValueLayoutData());
  // panel.addComponent(new Label("Accessible"), getDetailLayoutData());
  // }
  //
  // private void addDirectoryInfo(String label, Callable<File> directoryProvider, Panel panel) {
  // try {
  // File directory = directoryProvider.call();
  // addDirectoryInfo(label, directory, panel);
  // } catch (Exception e) {
  // addInfo(label, String.format("Error: %s", e.toString()), "no", panel);
  // }
  // }
  //
  // private void addDirectoryInfo(String label, File directory, Panel panel) {
  // String absolutePath = directory.getAbsolutePath();
  // boolean accessible = LimbusUtil.isAccessibleDirectory(directory);
  //
  // addInfo(label, absolutePath, toYesNo(accessible), panel);
  // }
  //
  // private String toYesNo(boolean accessible) {
  // return accessible ? "yes" : "no";
  // }
  //
  // private void addInfo(String label, String value, String detail, Panel panel) {
  // panel.addComponent(new Label(label), getLabelLayoutData());
  // panel.addComponent(new Label(value), getValueLayoutData());
  // panel.addComponent(new Label(detail), getDetailLayoutData());
  // }
  //
  // private LayoutData getLabelLayoutData() {
  // return GridLayout.createLayoutData(Alignment.END, Alignment.CENTER);
  // }
  //
  // private LayoutData getValueLayoutData() {
  // return GridLayout.createLayoutData(Alignment.FILL, Alignment.CENTER);
  // }
  //
  // private LayoutData getDetailLayoutData() {
  // return GridLayout.createLayoutData(Alignment.CENTER, Alignment.CENTER);
  // }

}
