package org.max5.limbus.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class PatternSpecifityComparatorTest {

  private static final String EMPTY = "";
  private static final String LESS_SPECIFIC = "a.b.c";
  private static final String SPECIFIC = "a.b.c.d.e";

  @Test
  public void test_order_by_specifity_with_list() {
    List<Pattern> toBeOrdered = new ArrayList<>(10);
    String current = "a.b.c.d.e.f.g";
    while (current.length() > 1) {
      toBeOrdered.add(new Pattern(current));
      current = current.substring(0, current.length() - 2);
    }

    Collections.shuffle(toBeOrdered);

    Collections.sort(toBeOrdered);

    Pattern last = null;
    Iterator<Pattern> it = toBeOrdered.iterator();
    while (it.hasNext()) {
      if (last == null) {
        last = it.next();
      } else {
        Pattern next = it.next();
        assertTrue(PatternSpecifityComparator.compareSpecifity(last, next) < 0);
        last = next;
      }
    }

    System.out.println(toBeOrdered);

  }

  @Test
  public void test() {
    int compareSpecifity;

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(SPECIFIC), new Pattern(LESS_SPECIFIC));
    assertTrue(compareSpecifity > 0);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(LESS_SPECIFIC), new Pattern(SPECIFIC));
    assertTrue(compareSpecifity < 0);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(SPECIFIC), new Pattern(LESS_SPECIFIC));
    assertFalse(compareSpecifity < 0);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(LESS_SPECIFIC), new Pattern(SPECIFIC));
    assertFalse(compareSpecifity > 0);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(SPECIFIC), new Pattern(SPECIFIC));
    assertEquals(0, compareSpecifity);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(SPECIFIC), new Pattern(EMPTY));
    assertTrue(compareSpecifity > 0);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(EMPTY), new Pattern(SPECIFIC));
    assertTrue(compareSpecifity < 0);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(SPECIFIC), new Pattern(EMPTY));
    assertFalse(compareSpecifity < 0);

    compareSpecifity = PatternSpecifityComparator.compareSpecifity(new Pattern(EMPTY), new Pattern(SPECIFIC));
    assertFalse(compareSpecifity > 0);

  }

}
