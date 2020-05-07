package com.remondis.limbus.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
