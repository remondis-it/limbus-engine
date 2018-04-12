package com.remondis.limbus;

import java.security.Permission;
import java.util.HashSet;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.remondis.limbus.launcher.EngineLauncher;
import com.remondis.limbus.launcher.EngineUtil;
import com.remondis.limbus.launcher.SystemEngine;

/**
 * This class is used to launch the Limbus Showcase examples by bootstrapping a
 * {@link LimbusEngine}. The shared classpath contains the centralized Log4J
 * dependency. The deploy folder contains the showcase plugin dependency that is
 * deployed at startup.
 *
 * <b>Note: The Log4J library is requested by the showcase plugin for testing
 * and compatibility demonstration purposes. This does not mean that Log4J is
 * needed for the host engine to be present.</b>
 *
 * @author schuettec
 *
 */
public class EngineShowcaseLauncher {

  public static void main(String[] args) throws Exception {
    /*
     * Before bootstrapping the engine there is a possibility to setup the shared
     * classpath (lib classpath). This is useful if you want to setup the shared
     * classpath with a set of URLs instead of providing a filesystem classpath.
     *
     * To specify a custom shared classpath use the following:
     */

    // Set the shared classpath provider to actually use it:
    // Currently commented out because we provide a filesystem shared classpath
    // which is used as default.
    // LimbusEngine.sharedClassPathProvider = sharedClasspathProvider;

    // The following call bootstraps the Limbus Engine
    EngineLauncher.bootstrapLimbusSystem();

    /*
     * As a developer you may wish to have a little more control over the engines
     * lifecycle. For JUnit tests it is possible to get the instance of
     * LimbusContainer which is the currently running engine type. The method
     * EngineLauncher.getEngine() delivers the current instance. It is deprecated to
     * signal that this method should not be used by plugins running in the Limbus
     * Engine.
     *
     * As a developer you can rely on this method to be available for test purposes.
     */
    try {
      SystemEngine system = (SystemEngine) EngineLauncher.getEngine();
      LimbusEngine engine = system.getComponent(LimbusEngine.class);
      DeployService deployService = system.getComponent(DeployService.class);

      Set<Permission> permissions = new HashSet<Permission>();
      permissions.add(new PropertyPermission("*", "read,write"));
      permissions.add(new java.lang.RuntimePermission("getClassLoader"));
      String version = EngineUtil.getEngineVersion();
      // Deploy Limbus Showcase Plugin Maven Artifact
      deployService.deployMavenArtifact("com.remondis.limbus", "limbus-showcase-plugin", null, version, permissions);
      // Find the plugin's classpath which identifies the plugin.
      String deployName = deployService.toDeployName("com.remondis.limbus", "limbus-showcase-plugin", null, version);
      Classpath classpath = engine.getClasspath(deployName);

      // Get the first plugin instance with specified lifecycle hook. The lifecycle
      // hook is executed on first plugin
      // request.
      String uuid = UUID.randomUUID()
          .toString();
      ExtendedLimbusPlugin plugin = engine.getPlugin(classpath, "limbus.showcase.plugin.ExtendedLimbusPluginImpl",
          ExtendedLimbusPlugin.class, new LimbusLifecycleHook<ExtendedLimbusPlugin>() {
            @Override
            public void preInitialize(ExtendedLimbusPlugin limbusPlugin) throws Exception {
              limbusPlugin.setObject(uuid);
            }

            @Override
            public void postFinish(ExtendedLimbusPlugin limbusPlugin) {
              limbusPlugin.setObject(null);
            }
          });

      // Periodically try to access the plugin instance.
      boolean run = true;
      while (run) {
        try {
          Thread.sleep(2000);
          System.out.printf("Plugin is initialized for %d seconds.\n",
              TimeUnit.MILLISECONDS.toSeconds(plugin.getRuntimeInMilliseconds()));
          System.out.println("Object reference: " + plugin.getObject());
          System.out.println("Try to undeploy the plugin via maintenance console.");

        } catch (PluginUndeployedException e) {
          System.out.println("The plugin was undeployed - stopping periodic plugin access.");
          run = false;
        }
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
