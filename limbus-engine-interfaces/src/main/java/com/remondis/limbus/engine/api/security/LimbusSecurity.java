package com.remondis.limbus.engine.api.security;

import java.security.Permission;
import java.util.Set;

import com.remondis.limbus.api.IInitializable;

/**
 * This is the Limbus Security component managing the default sandbox permissions for plugins.
 *
 * @author schuettec
 *
 */
public interface LimbusSecurity extends IInitializable<Exception> {
  /**
   * @return Returns the set of default permissions configured for this Limbus Engine instance to be used for the plugin
   *         classloaders.
   */
  public Set<Permission> getSandboxDefaultPermissions();

  /**
   * @return Returns the set of default permissions configured for this Limbus Engine instance to be used for the shared
   *         classloader.
   */
  public Set<Permission> getSharedClasspathDefaultPermissions();
}
