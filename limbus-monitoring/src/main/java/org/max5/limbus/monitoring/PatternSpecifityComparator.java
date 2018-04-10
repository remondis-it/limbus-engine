package org.max5.limbus.monitoring;

import java.util.Comparator;

import org.max5.limbus.utils.Lang;

public class PatternSpecifityComparator implements Comparator<Pattern> {

  @Override
  public int compare(Pattern o1, Pattern o2) {
    return compareSpecifity(o1, o2);
  }

  public static int compareSpecifity(Pattern o1, Pattern o2) {
    Lang.denyNull("o1", o1);
    Lang.denyNull("o2", o2);
    if (o1.getPattern()
        .length() < o2.getPattern()
            .length()) {
      return -1;
    } else if (o1.getPattern()
        .length() > o2.getPattern()
            .length()) {
      return 1;
    } else {
      return 0;
    }
  }

}
