package com.remondis.limbus.monitoring;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.utils.Files;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * This is the default Limbus Monitoring activator that searches for a valid monitoring configuration and initializes
 * the monitoring facade. The configuration is determined by the following strategy:
 * <ol>
 * <li>Try to find a valid configuration in conf/-directory. The system searches for a <tt>monitoring.xml</tt> in the
 * Limbus default configuration directory.</li>
 * <li>If nothing was found the classpath is searched for a <tt>monitoring.xml</tt></li>
 * <li>If still nothing was found, the Limbus Monitoring Facade defaults to a no-op state.</li>
 * </ol>
 *
 * @author schuettec
 *
 */
public class LimbusMonitoringActivator implements MonitoringActivator {

  private static final Logger log = LoggerFactory.getLogger(LimbusMonitoringActivator.class);

  @Override
  public void initializeMonitoring() {
    File confDir = Files.getConfigurationDirectoryUnchecked();
    File monitoringConf = new File(confDir, Conventions.DEFAULT_CONFIG_CLASSPATH);
    if (Files.isAccessibleFile(monitoringConf)) {
      try {
        MonitoringFactory.configureMonitoring(monitoringConf.toURI()
            .toURL());
      } catch (Exception e) {
        log.info("The monitoring could not be loaded due to a corrupt configuration.", e);
      }
    } else {
      ClassLoader classLoader = ReflectionUtil.getClassLoader(LimbusMonitoringActivator.class);
      URL resource = classLoader.getResource(Conventions.DEFAULT_CONFIG_CLASSPATH);
      if (resource == null) {
        log.info("The Limbus Monitoring Facade was not configured - defaulting to no-op.");
      } else {
        MonitoringFactory.configureMonitoring(resource);
      }
    }
  }

  @Override
  public void finishMonitoring() {
    MonitoringFactory.shutdown(true);
  }

}
