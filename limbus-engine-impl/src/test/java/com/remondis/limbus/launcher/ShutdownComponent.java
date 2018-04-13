package com.remondis.limbus.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.LimbusServicePlugin;

/**
 * This component is for developing and testing purposes only. This component waits for a user input via System.in. On
 * the next new line character, the component shuts down the engine. This component is not intended for use in
 * production environments as it is not common for runtime containers to shutdown.
 *
 * @author schuettec
 *
 */
public class ShutdownComponent extends LimbusServicePlugin {

  private static final Logger log = LoggerFactory.getLogger(ShutdownComponent.class);

  public ShutdownComponent() {
    log.info("SystemComponentTest created.");
  }

  @Override
  protected void performInitialize() throws Exception {
    log.info("SystemComponentTest initialized.");
  }

  @Override
  protected void performFinish() {
    log.info("SystemComponentTest finished.");
  }

  @Override
  public void run() {
    // NOTE: Avoid using Scanner here. There are known bugs in closing input streams in conjunction with multithreading.
    // To avoid this known java bug, we read the input stream ourselves.
    System.out.print("To terminate press ENTER: ");

    while (isRunning()) {
      try {
        if (System.in.available() > 0) {
          int read = System.in.read();
          if (read == '\r') {
            EngineLauncher.shutdownEngine();// Yes we understand that we should not use EngineLauncher.getEngine() if we
                                            // are a plugin or everything else than a developer testing stuff.
            break;
          }
        }

        Thread.sleep(200);
      } catch (InterruptedException e) {
        Thread.currentThread()
            .interrupt();
      } catch (Exception e) {
        // This can only occur if the process input pipe is broken, in this case shut down the system.
        EngineLauncher.shutdownEngine();// Yes we understand that we should not use EngineLauncher.getEngine() if we are
                                        // a plugin or everything else than a developer testing stuff.
      }
    }

  }

}
