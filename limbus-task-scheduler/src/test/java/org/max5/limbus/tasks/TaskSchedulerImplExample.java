package org.max5.limbus.tasks;

import java.util.function.Function;

public class TaskSchedulerImplExample implements Task {

  private long lastRun = 0;
  private long runs = 0;

  private static Function<Boolean, Long> scheduleRateFunction = new Function<Boolean, Long>() {

    private int unsuccessfullCounter = 0;

    @Override
    public Long apply(Boolean success) {
      if (success) {
        unsuccessfullCounter = 0;
      } else {
        unsuccessfullCounter++;
      }
      return (unsuccessfullCounter * 500l) + 1000;
    }
  };

  public static void main(String... args) throws Exception {
    TaskSchedulerImpl scheduler = new TaskSchedulerImpl();
    try {
      scheduler.initialize();
      scheduler.schedulePeriodicTask(new TaskSchedulerImplExample(), scheduleRateFunction);
    } finally {
      scheduler.finish();
    }

  }

  @Override
  public void execute() throws Exception {
    runs++;

    if (lastRun == 0) {
      lastRun = System.currentTimeMillis();
      System.out.println("First run executed.");
    } else {
      long time = System.currentTimeMillis() - lastRun;
      System.out.printf("Run %d - Executed after %dms.\n", runs, time);
      lastRun = System.currentTimeMillis();
    }

    if (runs >= 4 && runs < 8) {
      throw new Exception("Thrown for test purposes.");
    } else if (runs >= 8) {

    }

  }

}
