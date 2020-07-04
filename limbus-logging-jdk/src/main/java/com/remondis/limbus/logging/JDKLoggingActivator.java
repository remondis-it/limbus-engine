package com.remondis.limbus.logging;

import static com.remondis.limbus.utils.Files.getConfigurationDirectoryUnchecked;
import static com.remondis.limbus.utils.Files.isAccessibleFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.LogManager;

import com.remondis.limbus.activators.logging.LoggingActivator;
import com.remondis.limbus.activators.logging.LoggingActivatorException;
import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.utils.Lang;

/**
 * The JDK logging activator loads a default configuration to log to a logfile in <tt>logs/</tt> of the running Limbus
 * Engine. The configuration can be overridden by the file <tt>conf/logging.properties</tt> which should have the format
 * defined by the Java Utils Logging framework.
 *
 * 
 *
 */
public class JDKLoggingActivator extends Initializable<LoggingActivatorException> implements LoggingActivator {

  private static final String LOGGING_PROPERTIES = "host-logging.properties";

  @Override
  protected void performInitialize() throws LoggingActivatorException {
    try {
      // First try to access the logging.properties in Limbus Engine's conf/ folder.
      File confDir = getConfigurationDirectoryUnchecked();
      File logConfigOverride = new File(confDir, LOGGING_PROPERTIES);
      boolean override = isAccessibleFile(logConfigOverride);

      InputStream logConfig = null;
      if (override) {
        // Read from config file if accessible
        logConfig = new FileInputStream(logConfigOverride);
      } else {
        // Read the default logging configuration.
        logConfig = JDKLoggingActivator.class.getResourceAsStream("/" + LOGGING_PROPERTIES);

      }
      initializeJUL(logConfig);
    } catch (Exception e) {
      throw new LoggingActivatorException("Cannot initialize Java Utils Logging framework.", e);
    }
  }

  /**
   * Initializes the JDK logger and loads the configuration file from the specified stream.
   *
   * @throws Exception
   *         Thrown on any error.
   */
  private void initializeJUL(InputStream logConfig) throws Exception {
    Lang.denyNull("log configuration stream", logConfig);
    try {
      LogManager.getLogManager()
          .readConfiguration(logConfig);
    } catch (Exception e) {
      throw e;
    } finally {
      Lang.closeQuietly(logConfig);
    }
  }

  @Override
  protected void performFinish() {
    LogManager.getLogManager()
        .reset();
  }

}
