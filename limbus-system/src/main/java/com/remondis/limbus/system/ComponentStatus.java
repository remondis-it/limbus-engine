package com.remondis.limbus.system;

import com.remondis.limbus.utils.StringUtil;

/**
 * This enumeration defines all statuus a component can have.
 *
 * @author schuettec
 *
 */
public enum ComponentStatus {
  INITIALIZED,
  FINISHED,
  /**
   * Set if the component could not be initialized but is a required component.
   */
  ERROR,
  /**
   * Set if the component is an optional component and failed to initialize.
   */
  UNAVAILABLE;

  private static int maxCharCount = 0;

  static {
    for (ComponentStatus s : values()) {
      maxCharCount = Math.max(s.name()
          .length(), maxCharCount);
    }
  }

  /**
   * @return Returns the number of characters that are maximal needed to display status names.
   */
  public static int getMaxStringLength() {
    return maxCharCount;
  }

  /**
   * Does the same like {@link #toString()} but creates a string with as much characters as needed for the longest
   * status name.
   *
   * @return Returns this enum value as string.
   */
  public String toFormattedString() {
    return StringUtil.center(name(), maxCharCount);
  }

}
