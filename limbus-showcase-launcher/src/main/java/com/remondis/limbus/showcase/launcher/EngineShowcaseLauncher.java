package com.remondis.limbus.showcase.launcher;

import java.security.Permission;
import java.util.HashSet;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.engine.api.PluginDeployService;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.engine.api.LimbusLifecycleHook;
import com.remondis.limbus.engine.api.PluginUndeployedException;
import com.remondis.limbus.launcher.EngineLauncher;
import com.remondis.limbus.launcher.SystemEngine;

/**
 * This is a demonstration of bootstrapping a Limbus Engine. It will show how to
 * access the important components to get control over the running system. The
 * following lines will deploy a classpath with a specified set of permissions.
 * The deployment triggers the download of the plugin via Maven. Once the
 * classpath is deployed, a {@link LimbusLifecycleHook} is used to intercept the
 * plugin initialization. After this, the main thread will periodically check
 * the presence of the plugin instance. This will demonstrate, that the plugin
 * instance can be accesses as long as the classpath is deployed.
 *
 * You can undeploy the classpath at any time. The main thread will get a
 * {@link PluginUndeployedException} when accessing the plugin after
 * undeployment.
 *
 * 
 *
 */
public class EngineShowcaseLauncher {

  public static void main(String[] args) throws Exception {
    // The following call bootstraps the Limbus Engine
    EngineLauncher.bootstrapLimbusSystem(ShowcaseApplication.class);

    /*
     * As a developer you may wish to have a little more control over the engine's
     * lifecycle. For JUnit tests it is possible to get the instance of
     * LimbusContainer which is the currently running engine type. The method
     * EngineLauncher.getEngine() delivers the current instance.
     */
    try {
      // Get the component managing subsystem
      SystemEngine system = (SystemEngine) EngineLauncher.getEngine();
      // Get the Limbus Engine instance
      LimbusEngine engine = system.getComponent(LimbusEngine.class);
      // Get the deploy service for full control over deployed classpaths
      PluginDeployService deployService = system.getComponent(PluginDeployService.class);

      // Create a set of runtime permissions that will be granted to the plugin
      // classpath
      Set<Permission> permissions = new HashSet<Permission>();
      permissions.add(new PropertyPermission("*", "read,write"));
      permissions.add(new java.lang.RuntimePermission("getClassLoader"));
      // Deploy the plugin provided by this version
      String version = engine.getEngineVersion();
      // Deploy Limbus Showcase Plugin Maven Artifact
      deployService.deployMavenArtifact("com.remondis.limbus", "limbus-showcase-plugin", null, version, permissions);
      // Find the deployname which identifies the plugin classpath.
      String deployName = deployService.toDeployName("com.remondis.limbus", "limbus-showcase-plugin", null, version);
      // Get the classpath
      Classpath classpath = engine.getClasspath(deployName);

      // Get the first plugin instance with specified lifecycle hook. The lifecycle
      // hook is executed on first plugin access.
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
          // Demonstrate some calls to the plugin instance
          System.out.printf("Plugin is initialized for %d seconds.\n",
              TimeUnit.MILLISECONDS.toSeconds(plugin.getRuntimeInMilliseconds()));

          // Demonstrate that the lifecycle hook set a reference
          System.out.println("Object reference: " + plugin.getObject());
          System.out.println("Try to undeploy the plugin via maintenance console.");

        } catch (PluginUndeployedException e) {
          // This happens if the plugin gets undeployed. This exception is a runtime
          // exception.
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
