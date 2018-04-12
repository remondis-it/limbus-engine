package com.remondis.limbus.security;

import java.io.InputStream;
import java.net.URL;
import java.security.Permission;
import java.security.Policy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.remondis.limbus.Initializable;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.system.LimbusComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains methods to enable the plugin sandbox security framework that manages different sets of
 * permissions for code executed by the Limbus Engine and the plugins loaded by different classloaders.
 *
 * @author schuettec
 *
 */
public class LimbusSecurityImpl extends Initializable<Exception> implements LimbusSecurity {

  private static final Logger log = LoggerFactory.getLogger(LimbusSecurity.class);

  /**
   * The classpath file path for the sandbox default permission file.
   */
  private static final String SANDBOX_DEFAULT_PERMISSIONS = "sandbox_default.permissions";

  /**
   * Holds the security manager that was present before calling {@link #installSecurity()}
   */
  private SecurityManager systemSecurityManager = null;
  /**
   * Holds the policy that was present before calling {@link #installSecurity()}
   */
  private Policy systemPolicy = null;

  /**
   * Holds the set of default sandbox permissions. The permission file for the sandbox default permissions is read from
   * the classpath.
   */
  private Set<Permission> defaultPermissions = new HashSet<Permission>();

  @LimbusComponent
  private LimbusFileService filesystem;

  /**
   * This method installs a {@link SecurityManager} and a {@link Policy} to sandbox plugins and manage different sets of
   * permissions for the Limbus engine and plugins running.
   */
  private final void installSecurity() {
    systemPolicy = Policy.getPolicy();
    systemSecurityManager = System.getSecurityManager();

    // Set the default policy granting all access
    grantFullApplicationAccess();

    // Read sandbox default permissions.
    readSandboxDefaultPermissions();

    LimbusSecurityPolicy limbusPolicy = new LimbusSecurityPolicy();
    Policy.setPolicy(limbusPolicy);
    System.setSecurityManager(new LimbusSecurityManager(filesystem));

  }

  private void grantFullApplicationAccess() {
    // Load the engine.policy to grant full access for the application
    try {
      URL resource = LimbusSecurity.class.getResource("engine.policy");
      System.setProperty("java.security.policy", resource.toURI()
          .toURL()
          .toString());
      systemPolicy.refresh();
    } catch (Exception e) {
      log.error(
          "Could not load the engine.policy file grantin all access for Limbus Engine. Access denied errors may follow.",
          e);
    }
  }

  /**
   * @return Returns the default sandbox permissions defined by Limbus security.
   */
  @Override
  public final Set<Permission> getSandboxDefaultPermissions() {
    return Collections.unmodifiableSet(new HashSet<>(defaultPermissions));
  }

  /**
   * Reads the sandbox default permission file from classpath and holds this permission object for later use. Any error
   * is logged and an empty {@link Permission} set is created to grant no permisssions.
   */
  private final void readSandboxDefaultPermissions() {
    InputStream sandboxDefaultInput = LimbusSecurity.class.getResourceAsStream(SANDBOX_DEFAULT_PERMISSIONS);
    if (sandboxDefaultInput == null) {
      log.error(String.format("The default sandbox permission file was not found on classpath '%s'.",
          SANDBOX_DEFAULT_PERMISSIONS));
    } else {
      try (PermissionFileReader reader = new PermissionFileReader(sandboxDefaultInput)) {
        defaultPermissions = reader.getPermissions();

        logDefaultPermissions(defaultPermissions);

      } catch (PermissionCreateException e) {
        log.error("Cannot load the default sandbox permissions from classpaht.", e);
      }
    }
  }

  private void logDefaultPermissions(Set<Permission> permissions) {
    if (log.isDebugEnabled()) {
      if (permissions.isEmpty()) {
        log.debug(
            "Granting no sandbox permissions by default for Limbus plugins. Edit classpath file {} to change this.",
            SANDBOX_DEFAULT_PERMISSIONS);
      } else {
        log.debug(
            "Granting the following permissions by default for Limbus plugins. Edit classpath file {} to change this.",
            SANDBOX_DEFAULT_PERMISSIONS);
        for (Permission p : permissions) {
          log.debug("- Granting permission: {}", p.toString());
        }
      }
    }
  }

  public final void deinstallSecurity() {
    System.setSecurityManager(systemSecurityManager);
    Policy.setPolicy(systemPolicy);
  }

  @Override
  protected void performInitialize() throws Exception {
    installSecurity();
  }

  @Override
  protected void performFinish() {
    deinstallSecurity();
  }

}
