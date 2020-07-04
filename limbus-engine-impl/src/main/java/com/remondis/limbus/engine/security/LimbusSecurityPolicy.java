package com.remondis.limbus.engine.security;

import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Set;

import com.remondis.limbus.engine.PluginClassLoader;

/**
 * This is the Limbus security policy implementation that manages the sets of permissions granted for different
 * execution environments.
 *
 * 
 *
 */
class LimbusSecurityPolicy extends Policy {

  @Override
  public PermissionCollection getPermissions(ProtectionDomain domain) {
    // Decide if the plugin permissions are needed or full access can be granted using all permissions.
    if (isPlugin(domain)) {
      return pluginPermissions(domain);
    } else {
      return applicationPermissions();
    }
  }

  private boolean isPlugin(ProtectionDomain domain) {
    return domain.getClassLoader() instanceof PluginClassLoader;
  }

  private Permissions getDefaultPermissions(ProtectionDomain domain) {
    PluginClassLoader classloader = (PluginClassLoader) domain.getClassLoader();
    Set<Permission> permissions = classloader.getPermissions();
    Permissions permissionCollection = new Permissions();
    for (Permission p : permissions) {
      permissionCollection.add(p);
    }
    return permissionCollection;
  }

  private PermissionCollection pluginPermissions(ProtectionDomain domain) {
    // schuettec - 08.03.2017 : We return the permissions defined by the PluginClassloader here. There were errors in
    // conjunction with ClassLoader.defineClass() where the default protection domain grants no permissions at all. This
    // default protected domain will be overridden by the permissions returned here so the set of permissions applies
    // right on defineClass().
    return getDefaultPermissions(domain);
  }

  private PermissionCollection applicationPermissions() {
    // schuettec - 12.10.2016 : Always create a new permission object! It must be mutable and exclusive.
    // See documentation of com.remondis.limbus.security.LimbusSecurityPolicy.getPermissions(ProtectionDomain)
    // Permissions permissions = new Permissions();
    // schuettec - 13.10.2016 : It makes no sense to delegate to systemPolicy because it was already resolved when
    // creating the AppClassloader.
    Permissions permissions = new Permissions();
    permissions.add(new AllPermission());
    return permissions;
  }
}