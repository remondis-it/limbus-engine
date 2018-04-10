package org.max5.limbus.security;

import java.security.Permission;
import java.util.Set;

import org.max5.limbus.IInitializable;

/**
 * This is the Limbus Security component managing the default sandbox permissions for plugins.
 *
 * @author schuettec
 *
 */
public interface LimbusSecurity extends IInitializable<Exception> {
  /**
   * @return Returns the set of default permissions configured for this Limbus Engine instance.
   */
  public Set<Permission> getSandboxDefaultPermissions();
}
