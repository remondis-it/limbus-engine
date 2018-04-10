package limbus.showcase.plugin;

import org.apache.log4j.Logger;
import org.max5.limbus.LimbusServicePlugin;

public class TestLimbusService extends LimbusServicePlugin {

  private static final Logger log = Logger.getLogger(TestLimbusService.class);

  @Override
  public void run() {
    log.info("Limbus Showcase Service Plugin is running its thread....");
    while (isRunning()) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        Thread.currentThread()
            .interrupt();
      }
    }
    log.info("Limbus Showcase Service Plugin - Thread was finished gently.");
  }

  @Override
  protected void performInitialize() throws Exception {
    log.info("Limbus Showcase Service Plugin was initialized.");
  }

  @Override
  protected void performFinish() {
    log.info("Limbus Showcase Service Plugin was finished.");
  }

}
