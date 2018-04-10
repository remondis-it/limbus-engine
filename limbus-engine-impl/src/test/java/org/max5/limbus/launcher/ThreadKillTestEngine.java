package org.max5.limbus.launcher;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.max5.limbus.IInitializable;

public class ThreadKillTestEngine extends SimpleEngine {

  @Override
  public IInitializable<?>[] createSystemComponents() {

    final AtomicBoolean run = new AtomicBoolean(true);
    final Semaphore waitUntilStarted = new Semaphore(1);
    waitUntilStarted.acquireUninterruptibly();

    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        waitUntilStarted.release();
        // Provoke a dirty shutdown
        while (run.get()) {
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
            // This time ignore interrupts
          }
        }
      }
    }, "THIS_THREAD_CAN_ONLY_BE_KILLED_USING-STOP()");

    // Start the thread out of the scope of ThreadSnapshot
    t.start();
    waitUntilStarted.acquireUninterruptibly();
    waitUntilStarted.release();

    return new IInitializable<?>[] {};
  }

}
