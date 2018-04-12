package com.remondis.limbus.files;

import static com.remondis.limbus.utils.Lang.*;

import java.io.File;
import java.util.regex.Pattern;

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
      // Remove all file separators to get a normalized path.
      String segment = pathSegments[i].replaceAll(Pattern.quote(File.separator), "");
      path.append(segment);
      if (i < pathSegments.length - 1) {
        path.append(File.separator);
      }
    }
    return path.toString();
  }

}
