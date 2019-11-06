package com.remondis.limbus.launchertest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.remondis.limbus.engine.NoOpEngine;
import com.remondis.limbus.engine.api.EmptySharedClasspathProvider;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.engine.api.LogTarget;
import com.remondis.limbus.engine.api.SharedClasspathProvider;
import com.remondis.limbus.engine.api.security.LimbusSecurity;
import com.remondis.limbus.engine.logging.FileSystemLogTarget;
import com.remondis.limbus.engine.security.LimbusSecurityImpl;
import com.remondis.limbus.files.InMemoryFilesystemImpl;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.launcher.Engine;
import com.remondis.limbus.launcher.EngineLaunchException;
import com.remondis.limbus.launcher.EngineLauncher;
import com.remondis.limbus.launcher.SystemEngine;
import com.remondis.limbus.system.LimbusSystem;

public class EngineLauncherTest extends EngineLauncher {

  private boolean skipSystemExitOLD;

  private LimbusSystem system;

  @Before
  public void setup() {
    skipSystemExitOLD = EngineLauncher.skipSystemExit;
    EngineLauncher.skipSystemExit = true;

    this.system = new LimbusSystem();
    this.system.addComponentConfiguration(LimbusSecurity.class, LimbusSecurityImpl.class);
    this.system.addComponentConfiguration(SharedClasspathProvider.class, EmptySharedClasspathProvider.class);
    this.system.addComponentConfiguration(LimbusFileService.class, InMemoryFilesystemImpl.class);
    this.system.addComponentConfiguration(LogTarget.class, FileSystemLogTarget.class);
    this.system.addComponentConfiguration(LimbusEngine.class, NoOpEngine.class);
  }

  @After
  public void tearDown() {
    EngineLauncher.skipSystemExit = skipSystemExitOLD;
  }

  @Test
  public void test_limbus_engine() throws Exception {
    EngineLauncher.bootstrapLimbusSystem(system);
    try {
      SystemEngine engine = (SystemEngine) EngineLauncher.getEngine();
      assertNotNull(engine.getComponent(LimbusSecurity.class));
      assertNotNull(engine.getComponent(SharedClasspathProvider.class));
      assertNotNull(engine.getComponent(LogTarget.class));
      assertNotNull(engine.getComponent(LimbusFileService.class));
      assertNotNull(engine.getComponent(LimbusEngine.class));

    } finally {
      EngineLauncher.shutdownEngine();
      EngineLauncher.waitForShutdown();
    }
  }

  @Test
  public void test_call_shutdown_without_bootstrap() {
    EngineLauncher.shutdownEngine();
    EngineLauncher.waitForShutdown();

    assertFalse(EngineLauncher.lastShutdownWasDirty);
  }

  @Test
  public void test_bootstrap_twice_and_concurrently() throws Exception {

    EngineLauncher.bootstrapLimbusSystem(system);
    try {
      EngineLauncher.bootstrapLimbusSystem(system);
      fail("EngineLaunchException was expected but not thrown.");
    } catch (EngineLaunchException e) {
      // Totally expected.
    }

    EngineLauncher.shutdownEngine();
    EngineLauncher.waitForShutdown();

  }

  @Test
  public void test_call_shutdown_after_shutdown() {

    try {
      EngineLauncher.bootstrapLimbusSystem(system);
    } catch (Exception e) {
      // Keep this silent. This is expected.
      e.printStackTrace();
    }
    EngineLauncher.shutdownEngine();
    EngineLauncher.waitForShutdown();
    EngineLauncher.shutdownEngine();
    EngineLauncher.waitForShutdown();

    assertFalse(EngineLauncher.lastShutdownWasDirty);

  }

  /**
   * Ensures that the engine does not try to stop the thread created in this test due to specifying a thread snapshot.
   *
   * @throws Exception
   */
  @Test
  public void test_thread_snapshot_happy() throws Exception { // Happy path
    AtomicBoolean interruptThrown = new AtomicBoolean(false);

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
    t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

      @Override
      public void uncaughtException(Thread t, Throwable e) {
        interruptThrown.set(true);
      }
    });

    EngineLauncher.bootstrapLimbusSystem(system);
    Engine engine = EngineLauncher.getEngine();
    assertNotNull(engine);
    EngineLauncher.shutdownEngine();
    EngineLauncher.waitForShutdown();

    // There may not be an interrupt signal, because we wanted the engine to ignore the thread recorded by the thread
    // snapshot.
    assertFalse(interruptThrown.get());
    // The shutdown is not dirty because there should not have been threads created by the engine that do not terminate.
    assertFalse(EngineLauncher.lastShutdownWasDirty);
  }

  /**
   * Ensures that the engine detects a dirty shutdown. This is provocated by a thread that will not terminate on
   * interrupts.
   *
   * @throws Exception
   */
  @Test
  public void test_thread_snapshot() throws Exception {
    EngineLauncher.bootstrap(ThreadKillTestEngine.class);
    Engine engine = EngineLauncher.getEngine();
    assertNotNull(engine);
    EngineLauncher.shutdownEngine();
    EngineLauncher.waitForShutdown();
    assertTrue(EngineLauncher.lastShutdownWasDirty);
  }

}
