package com.remondis.limbus.tasks;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;

public class GentleRegenerationTest {

  private static final boolean FAIL = false;
  private static final boolean SUCCESS = true;

  private static final long INITIAL_WAIT = 4;
  private static final int ACCEPTED_FAILS = 5;

  private static final int AFTER_FAILS = 10;
  private static final int MAX_WAIT = 60;

  private static final int REGEN_AFTER_SUCCESS = 5;

  @Test // Happy path
  public void test() {

    GentleRegeneration s = new GentleRegeneration(INITIAL_WAIT, ACCEPTED_FAILS, AFTER_FAILS, MAX_WAIT,
        REGEN_AFTER_SUCCESS);

    // Check ACCEPTED_FAILS times success
    times(ACCEPTED_FAILS, assertWait(s, INITIAL_WAIT), SUCCESS);

    // Check ACCEPTED_FAILS times fails
    times(ACCEPTED_FAILS, assertWait(s, INITIAL_WAIT), FAIL);

    // Check more failures. The wait time must raise.
    times(AFTER_FAILS - 1, assertHigherWaitThan(s, INITIAL_WAIT), FAIL);

    // Check that the MAX_WAIT is reached and does not exceed MAX_FQ
    times(AFTER_FAILS, assertWait(s, MAX_WAIT), FAIL);

    // Regenerate and ensure that the wait decreases
    times(REGEN_AFTER_SUCCESS - 1, assertLowerWaitThan(s, MAX_WAIT), SUCCESS);

    // Ensure that the wait is regenerated on this success
    times(1, assertWait(s, INITIAL_WAIT), SUCCESS);

    // Check ACCEPTED_FAILS times success
    times(ACCEPTED_FAILS, assertWait(s, INITIAL_WAIT), SUCCESS);
  }

  private Consumer<Boolean> assertLowerWaitThan(GentleRegeneration s, long frequency) {
    return (r) -> {
      long actualFq = s.apply(r);
      assertTrue(frequency > actualFq);
    };
  }

  private Consumer<Boolean> assertHigherWaitThan(GentleRegeneration s, long waitTime) {
    return (r) -> {
      long actualWait = s.apply(r);
      assertTrue(waitTime < actualWait);
    };
  }

  private Consumer<Boolean> assertWait(GentleRegeneration s, long assertWait) {
    return (r) -> {
      long actualFq = s.apply(r);
      assertEquals(assertWait, actualFq);
    };
  }

  @SafeVarargs
  static <T> void times(long iterations, Consumer<T> consumer, T... accept) {
    for (int i = 0; i < iterations; i++) {
      int index = i;
      T toAccept = null;
      if (index >= accept.length) {
        index %= accept.length;
      }
      toAccept = accept[index];
      consumer.accept(toAccept);
    }
  }
}
