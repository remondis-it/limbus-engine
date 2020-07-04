package com.remondis.limbus.engine.security;

import static com.remondis.limbus.engine.security.PermissionUtil.arguments;
import static com.remondis.limbus.engine.security.PermissionUtil.replaceSystemProperties;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Permission;
import java.util.Set;

/**
 * This class is able to read permission files. A permission file is a file that enumerates permissions per line. Use
 * the following syntax in permission files:
 * <p>
 * <tt>
 * permissionClass name actions<br/>
 * permissionClass name actions<br/>
 * permissionClass name actions<br/>
 * </tt>
 * </p>
 * <p>
 * Make sure that permissions are separated by new line character.
 * </p>
 *
 * <p>
 * <b>Note: Permission files read by this reader may contain comments. Comments lines are declared by starting with #
 * character.</b>
 * </p>
 *
 * 
 *
 */
public class PermissionFileReader implements Closeable {

  private static final String PREFIX_COMMENT = "#";
  private BufferedReader reader;

  public PermissionFileReader(InputStream input) {
    this.reader = new BufferedReader(new InputStreamReader(input));
  }

  /**
   * Reads the permissions from the input source and creates a set of permissions.
   *
   * @return Returns the set of permissions.
   * @throws PermissionCreateException
   *         Thrown on any error while reading the input source or creating the permission.
   */
  public Set<Permission> getPermissions() throws PermissionCreateException {
    PermissionBuilder builder = PermissionBuilder.create();
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        // Skip comment lines
        if (!line.startsWith(PREFIX_COMMENT) && !line.trim()
            .isEmpty()) {
          // Replace key with system property values
          line = replaceSystemProperties(line);
          // Get the first two arguments
          String[] args = arguments(line, 3);
          if (args.length != 3) {
            throw new PermissionCreateException(
                "Syntax error for permission declaration. Use the syntax 'permissionClass name actions'. Declaration was: "
                    + line);
          }
          builder.setPermissionClass(args[0])
              .setName(args[1])
              .setActions(args[2])
              .add();
        }
      }
    } catch (IOException e) {
      throw new PermissionCreateException("Cannot read the permission source due to I/O error.", e);
    }
    return builder.get();
  }

  @Override
  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      // Keep this silent.
    }
  }

}
