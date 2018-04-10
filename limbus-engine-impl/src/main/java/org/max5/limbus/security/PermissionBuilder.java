package org.max5.limbus.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;
import java.security.Permissions;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.max5.limbus.utils.Lang;
import org.max5.limbus.utils.ReflectionUtil;

/**
 * This util class helps to create a set of {@link Permission} objects to grant privileges to Limbus Plugins.
 *
 * @author schuettec
 *
 */
public final class PermissionBuilder {

  private String permissionClass;
  private String name;
  private String actions;

  private Set<Permission> permissions;

  PermissionBuilder() {
    this.permissions = new HashSet<Permission>();
  }

  /**
   * Creates a new {@link PermissionBuilder} to build a set of {@link Permissions}.
   *
   * @return Returns a new {@link PermissionBuilder}.
   */
  public static PermissionBuilder create() {
    return new PermissionBuilder();
  }

  /**
   * @param permissionClass
   *        Sets the permission class for the next permission.
   * @return Returns this object for method chaining.
   */
  public PermissionBuilder setPermissionClass(String permissionClass) {
    this.permissionClass = permissionClass;
    return this;
  }

  /**
   * @param name
   *        Sets the permission name for the next permission. <b>This method supports variable substitution in format
   *        '${sysPropKey}' that resolved to a system property with key 'sysPropKey'.</b>
   * @return Returns this object for method chaining.
   */
  public PermissionBuilder setName(String name) {
    this.name = PermissionUtil.replaceSystemProperties(name);
    return this;
  }

  /**
   * @param name
   *        Sets the permission name for the next permission. <b>This method does no variable substitution.</b>
   * @return Returns this object for method chaining.
   */
  public PermissionBuilder setNameUnresolved(String name) {
    this.name = name;
    return this;
  }

  /**
   * @param actions
   *        Sets the action for the next permission. <b>This method supports variable substitution in format
   *        '${sysPropKey}' that resolved to a system property with key 'sysPropKey'.</b>
   * @return Returns this object for method chaining.
   */
  public PermissionBuilder setActions(String actions) {
    this.actions = PermissionUtil.replaceSystemProperties(actions);
    return this;
  }

  /**
   * @param actions
   *        Sets the action for the next permission. <b>This method does no variable substitution.</b>
   * @return Returns this object for method chaining.
   */
  public PermissionBuilder setActionsUnresolved(String actions) {
    this.actions = actions;
    return this;
  }

  /**
   * Creates a {@link Permission} from the arguments {@link #permissionClass}, {@link #name} and {@link #actions}
   * previously set. Then this {@link PermissionBuilder} is ready to add the next {@link Permission}.
   * <p>
   * <b>
   * Note: The previously entered arguments are cleared.
   * </b>
   * </p>
   *
   * @return Returns this object for method chaining.
   * @throws PermissionCreateException
   *         Thrown if something went wrong during creation of the permission.
   */
  public PermissionBuilder add() throws PermissionCreateException {
    Lang.denyNull("permissionClass", permissionClass);
    Lang.denyNull("name", name);
    // Actions may be optional
    Permission permission = parsePermission(permissionClass, name, actions);
    this.permissions.add(permission);
    clear();
    return this;
  }

  /**
   * @return Returns the set of {@link Permission} objects created by this builder. <b>Note: After this operation, this
   *         builder is reset and should not be reused.</b>
   */
  public Set<Permission> get() {
    Set<Permission> immutable = Collections.unmodifiableSet(new HashSet<>(this.permissions));
    this.permissions = null;
    this.clear();
    return immutable;
  }

  private void clear() {
    this.permissionClass = null;
    this.name = null;
    this.actions = null;
  }

  /**
   * This method constructs a {@link Permission} object. The most common signature to construct permissions seems to be
   * <p>
   * <tt>permissionClass "name" "actions"</tt>
   * </p>
   * If a {@link Permission} uses other fields that those two string
   * arguments they are not supported for this version of Limbus Engine.
   *
   * @param permissionClass
   *        The permission class is assumed to be available in the application's classpath. <b>Note: The specified
   *        class cannot be loaded from the plugin classpath!</b>
   * @param name
   *        The name of the permission. Might also be used as parameter by some permission implementations.
   * @param actions
   *        The actions to be granted.
   * @return Returns always a new {@link Permission} object if successfully created.
   */
  private static Permission parsePermission(String permissionClass, String name, String actions)
      throws PermissionCreateException {
    try {
      // Use the application's class loader which is assumed to be the one that loaded this class.
      ClassLoader classLoader = PermissionBuilder.class.getClassLoader();
      Class<Permission> permissionType = ReflectionUtil.loadServiceClass(permissionClass, Permission.class,
          classLoader);
      Constructor<Permission> constructor = permissionType.getConstructor(String.class, String.class);
      Permission newInstance = constructor.newInstance(name, actions);
      return newInstance;
    } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException
        | ExceptionInInitializerError e) {
      throw new PermissionCreateException("Creating the permission instance failed with an exception.", e);
    } catch (NoSuchMethodException e) {
      throw new PermissionCreateException(
          "The permission object does not have the expected constructor: (String, String).");
    } catch (Exception e) {
      throw new PermissionCreateException("Cannot create permission.", e);
    }
  }

}
