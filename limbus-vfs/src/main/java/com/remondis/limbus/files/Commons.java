package com.remondis.limbus.files;

import static com.remondis.limbus.utils.Lang.denyNull;

import java.io.File;

/**
 * Commonly used methods of {@link InMemoryFilesystemImpl} and {@link FileSystemServiceImpl}.
 * 
 * @author schuettec
 *
 */
public class Commons {

  public static String toPath(String[] pathSegments) {
    denyNull("pathSegments", pathSegments);
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < pathSegments.length; i++) {
      String segment = pathSegments[i];
      path.append(segment);
      if (i < pathSegments.length - 1) {
        path.append(File.separator);
      }
    }
    return path.toString();
  }

}
