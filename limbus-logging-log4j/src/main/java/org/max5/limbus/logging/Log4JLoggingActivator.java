package org.max5.limbus.logging;

import static org.max5.limbus.utils.Files.getConfigurationDirectoryUnchecked;
import static org.max5.limbus.utils.Files.isAccessibleFile;

import java.io.File;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.max5.limbus.Initializable;
import org.max5.limbus.utils.Lang;

/**
 * The Log4j logging activator loads a default configuration to log to a logfile in <tt>logs/</tt> of the running Limbus
 * Engine. The configuration can be overridden by the file <tt>conf/host-logging-log4j.xml</tt> which should have the
 * format
 * defined by the Log4J framework.
 *
 * @author schuettec
 *
 */
public class Log4JLoggingActivator extends Initializable<LoggingActivatorException> implements LoggingActivator {
  // schuettec - 20.12.2016 : Do not use "log4j.xml" because in this case the conf/log4j.xml does not override the
  // classpath log4j.xml
  private static final String DEFAULT_LOG4J_XML = "host-logging-log4j.xml";

  @Override
  protected void performInitialize() throws LoggingActivatorException {
    try {
      // First try to access the logging.properties in Limbus Engine's conf/ folder.
      File confDir = getConfigurationDirectoryUnchecked();
      File logConfigOverride = new File(confDir, DEFAULT_LOG4J_XML);
      boolean override = isAccessibleFile(logConfigOverride);

      URL logConfig = null;
      if (override) {
        // Read from config file if accessible
        logConfig = logConfigOverride.toURI()
            .toURL();
      } else {
        // Read the default logging configuration.
        logConfig = Log4JLoggingActivator.class.getResource("/" + DEFAULT_LOG4J_XML);

      }
      initializeLog4J(logConfig);
    } catch (Throwable t) {
      throw new LoggingActivatorException("Cannot initialize Log4J logging framework.", t);
    }
  }

  /**
   * Initializes the JDK logger and loads the configuration file from the specified stream.
   *
   * @throws Exception
   *         Thrown on any error.
   */
  private void initializeLog4J(URL logConfigURL) throws Throwable {
    Lang.denyNull("log configuration URL", logConfigURL);
    try {
      DOMConfigurator.configure(logConfigURL);
    } catch (Throwable t) {
      throw t;
    }
  }

  @Override
  protected void performFinish() {
    LogManager.resetConfiguration();
    LogManager.shutdown();
  }

}
