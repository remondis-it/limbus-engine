package limbus.showcase.plugin;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.max5.limbus.ExtendedLimbusPlugin;
import org.max5.limbus.Initializable;

public class ExtendedLimbusPluginImpl extends Initializable<Exception> implements ExtendedLimbusPlugin {

  private static final Logger log = Logger.getLogger(ExtendedLimbusPluginImpl.class);

  private long initializedTimestamp = 0;

  private Object object;

  @Override
  protected void performInitialize() throws Exception {
    log.info("Extended Limbus Plugin was initialized.");
    this.initializedTimestamp = System.currentTimeMillis();
  }

  @Override
  protected void performFinish() {
    log.info("Extended Limbus Plugin was finished.");
  }

  @Override
  public long getRuntimeInMilliseconds() {
    checkState();
    return System.currentTimeMillis() - initializedTimestamp;
  }

  @Override
  public void setObject(Object object) {
    this.object = object;
  }

  @Override
  public Object getObject() {
    return object;
  }

  @Override
  public void writeToSystemOut(byte[] message) throws IOException {
    System.out.write(message);
  }

  @Override
  public void writeToSystemErr(byte[] message) throws IOException {
    System.err.write(message);
  }

}
