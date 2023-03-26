package com.remondis.limbus.launchertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.remondis.limbus.launcher.ThreadSnapshot;

public class ThreadSnapshotTest {

  @Test // Happy path.
  public void test_thread_snapshot() throws Exception {
    ThreadSnapshot snapshot = ThreadSnapshot.snapshot();
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        while (!Thread.interrupted()) {
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
          }
        }
      }
    });
    t.start();
    ThreadSnapshot later = ThreadSnapshot.snapshot();

    assertFalse(snapshot.isEmpty());
    assertFalse(later.isEmpty());

    int snapshotSize = snapshot.size();
    int laterSize = later.size();

    assertEquals(snapshotSize + 1, laterSize);

    Set<Integer> difference = snapshot.difference(later);
    assertEquals(1, difference.size());

    Set<Thread> threads = later.getThreads();
    assertTrue(threads.contains(t));

    t.interrupt();
    t.join();

    threads = later.getThreads();
    assertFalse(threads.contains(t));

  }

}
