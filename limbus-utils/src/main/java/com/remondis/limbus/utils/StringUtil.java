package com.remondis.limbus.utils;

/**
 * This class provides string utilities used to format strings.
 * 
 * @author schuettec
 *
 */
public class StringUtil {

  public static String center(String s, int size) {
    return center(s, size, ' ');
  }

  public static String center(String s, int size, char pad) {
    if (s == null || size <= s.length())
      return s;

    StringBuilder sb = new StringBuilder(size);
    for (int i = 0; i < (size - s.length()) / 2; i++) {
      sb.append(pad);
    }
    sb.append(s);
    while (sb.length() < size) {
      sb.append(pad);
    }
    return sb.toString();
  }
}
