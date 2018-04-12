package limbus.showcase.plugin;

import org.apache.log4j.Logger;
import com.remondis.limbus.Initializable;
import com.remondis.limbus.LimbusPlugin;

public class TestLimbusPlugin extends Initializable<Exception> implements LimbusPlugin {

  private static final Logger log = Logger.getLogger(TestLimbusPlugin.class);

  public TestLimbusPlugin() {
  }

  public ClassLoader getClassloader() {
    return TestLimbusPlugin.class.getClassLoader();
  }

  public ClassLoader getThreadContext() {
    return Thread.currentThread()
        .getContextClassLoader();
  }

  @Override
  protected void performInitialize() throws Exception {
    log.info("Test Limbus Plugin initialized.");

    printClassLoaderHierarchy("Plugin class class loader", getClassloader());
    printClassLoaderHierarchy("Thread Context class loader", getThreadContext());

  }

  public Logger getLog() {
    return log;
  }

  private void printClassLoaderHierarchy(String string, ClassLoader threadContext) {
    String outString = null;
    ClassLoader cl = threadContext;
    do {
      if (outString == null) {
        outString = cl.toString();
      } else {
        outString += " -> " + cl.toString();
      }
    } while ((cl = cl.getParent()) != null);
    log.info(String.format("Printing out class loader hierarchy of %s: %s", string, outString));
  }

  @Override
  protected void performFinish() {
    log.info("Test Limbus Plugin fished.");
  }

}
