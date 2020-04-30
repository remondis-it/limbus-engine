package com.remondis.limbus.tasks;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.remondis.limbus.files.InMemoryFilesystemImpl;

@RunWith(MockitoJUnitRunner.class)
public class TaskSchedulerImplTest implements Task {

  Semaphore waitForExecution = new Semaphore(1);
  AtomicInteger calls = new AtomicInteger();
  TaskSchedulerImpl scheduler;

  @Test
  public void test() throws Exception {
    waitForExecution.acquire();

    InMemoryFilesystemImpl memFs = new InMemoryFilesystemImpl();
    scheduler = new TaskSchedulerImpl();
    scheduler.filesystem = memFs;
    scheduler.initialize();

    scheduler.schedulePeriodicTask(this, (b) -> {
      return 1L;
    });

    waitForExecution.acquire();
    waitForExecution.release();

    scheduler.finish();

    assertEquals(1, calls.get());
  }

  @Override
  public void execute() throws Exception {
    calls.incrementAndGet();
    scheduler.unschedulePeriodicTask(this);
    waitForExecution.release();
  }

}
