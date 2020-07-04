package com.remondis.limbus.engine.security;

import java.util.Properties;

/**
 * Util methods that are useful to parse permissions and its arguments.
 *
 * 
 *
 */
class PermissionUtil {

  private static final String ARGUMENT_SEPARATOR = "|";

  /**
   * Replaces system properties marked with <tt>${sysPropKey}</tt> by their actual values.
   *
   * @param parameter
   *        The string to replace system properties within.
   * @return Returns the string with replaces system properties.
   * @throws IllegalArgumentException
   *         Thrown if a system property variable cannot be resolved.
   */
  protected static String replaceSystemProperties(String parameter) throws IllegalArgumentException {
    if (parameter == null) {
      return null;
    } else {
      Properties properties = System.getProperties();
      while (parameter.contains("${")) {
        int startIndex = parameter.indexOf("${") + 2;
        int endIndex = parameter.indexOf("}");
        String key = parameter.substring(startIndex, endIndex);
        String replaceTag = String.format("${%s}", key);
        if (properties.containsKey(key)) {
          Object object = properties.get(key);
          parameter = parameter.replace(replaceTag, object.toString());
        } else {
          throw new IllegalArgumentException(
              String.format("System property '%s' could not be determined. Line was: %s", key, parameter));
        }
      }
      return parameter;
    }
  }

  /**
   * Extracts arguments from the string separated by {@value #ARGUMENT_SEPARATOR}. The number of arguments to be
   * interpreted can be set with
   * <tt>length</tt>.
   * <tt>
   *    Example line: arg1 arg2 arg3 arg4</br>
   * Result length=3: ["arg1", "arg2", "arg3 arg4"]
   * Result length=4: ["arg1", "arg2", "arg3", "arg4"]
   * </tt>
   *
   *
   * @param line
   *        The string
   * @param length
   *        The number of arguments to be extracted.
   * @return Returns the string with the interpreted arguments.
   */
  protected static String[] arguments(String line, int length) {
    if (length == 0)
      return new String[0];
    String[] args = new String[length];
    // Interpret arguments until length-1...
    int i = 0;
    for (i = 0; i < length - 1; i++) {
      int spaceIndex = line.indexOf(ARGUMENT_SEPARATOR);
      if (spaceIndex == -1) {
        break;
      } else {
        String argument = line.substring(0, spaceIndex);
        line = line.substring(spaceIndex + 1, line.length());
        args[i] = argument.trim();
      }
    }
    // and add the rest of the string as a single argument.
    args[i] = line.trim();
    return args;
  }

}
