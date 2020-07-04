package com.remondis.limbus.tasks;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * This is a scheduling rate function that lowers the scheduling frequency on exceptions. The regeneration on the
 * upcoming successful results
 *
 * 
 *
 */
public class GentleRegeneration implements Function<Boolean, Long> {

  private static final int DEFAULT_REGENERATE_IN = 4;
  private static final int DEFAULT_MAX_WAIT_SECONDS = 30;
  private static final int DEFAULT_MAX_FAILS = 10;
  private static final int DEFAULT_ACCEPTED_FAILS = 5;

  private long initialInterval;
  private long acceptedFails;
  private long maxFails;
  private long maxInterval;

  private Function<Double, Double> failFunction;
  private Function<Double, Double> regenerateFunction;

  private Function<Double, Double> MAX_FUNCTION = (f) -> {
    return Math.min(f, maxInterval);
  };

  private long fails = 0;

  /**
   * Creates a default polling scheduling function. The following configuration is used:
   * <ul>
   * <li>Initial wait time in seconds as specified.</li>
   * <li>After {@value #DEFAULT_ACCEPTED_FAILS} accepted fails the function will increase the wait time.</li>
   * <li>After {@value #DEFAULT_MAX_FAILS} fails the function will reach the maximum wait time of
   * {@value #DEFAULT_MAX_WAIT_SECONDS}
   * seconds.</li>
   * <li>The wait time will regenerate to the initial wait time after {@value #DEFAULT_REGENERATE_IN} succeeded
   * executions.</li>
   * </ul>
   *
   * @param initialIntervalSeconds
   *        The initial interval of the periodic task.
   * @return Returns a {@link GentleRegeneration} scheduling function for polling tasks targeting a database.
   */
  public static GentleRegeneration pollingDefault(long initialIntervalSeconds) {
    return new GentleRegeneration(TimeUnit.SECONDS.toMillis(initialIntervalSeconds), DEFAULT_ACCEPTED_FAILS,
        DEFAULT_MAX_FAILS, TimeUnit.SECONDS.toMillis(DEFAULT_MAX_WAIT_SECONDS), DEFAULT_REGENERATE_IN);
  }

  public GentleRegeneration(long initialInterval, long acceptedFails, long afterFails, long maxInterval,
      long initialIntervalAfterSuccess) {
    this.initialInterval = initialInterval;
    this.acceptedFails = acceptedFails;
    this.maxInterval = maxInterval;
    this.maxFails = afterFails;

    this.failFunction = new LinearInterpolator(acceptedFails, initialInterval, afterFails, maxInterval)
        .andThen(MAX_FUNCTION);
    this.regenerateFunction = new LinearInterpolator(afterFails, maxInterval, initialIntervalAfterSuccess,
        initialInterval).andThen(MAX_FUNCTION);
  }

  @Override
  public Long apply(Boolean success) {
    if (success) {
      decrementFails();
    } else {
      incrementFails();
    }

    if (success) {
      if (fails <= acceptedFails) {
        return initialInterval;
      } else {
        return Math.round(regenerateFunction.apply((double) fails));
      }
    } else {
      if (fails <= acceptedFails) {
        return initialInterval;
      } else {
        return Math.round(failFunction.apply((double) fails));
      }
    }
  }

  private void incrementFails() {
    fails++;
    fails = Math.min(fails, maxFails);
  }

  private void decrementFails() {
    fails--;
    fails = Math.max(fails, 0);
  }

}
