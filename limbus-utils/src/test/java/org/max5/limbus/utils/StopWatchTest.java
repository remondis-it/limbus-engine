package org.max5.limbus.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StopWatchTest {

  /**
   * This test checks that the caller information are correct for the different constructors.
   */
  @Test
  public void test_caller_information() {
    StopWatch w = new StopWatch();
    assertEquals(getClass().getName(), w.getClassName());
    assertEquals("test_caller_information", w.getMethod());

    w = new StopWatch("title");
    assertEquals(getClass().getName(), w.getClassName());
    assertEquals("test_caller_information", w.getMethod());

    w = new StopWatch("title", "description");
    assertEquals(getClass().getName(), w.getClassName());
    assertEquals("test_caller_information", w.getMethod());

  }

}
