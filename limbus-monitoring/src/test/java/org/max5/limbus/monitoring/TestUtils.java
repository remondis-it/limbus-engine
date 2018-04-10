package org.max5.limbus.monitoring;

import org.max5.limbus.utils.Lang;

public class TestUtils {

  public static void silentLog() {
    Lang.initializeJDKLogging(MonitoringFactoryTest.class.getResource("/silent-log.properties"));
  }

  public static void defaultLog() {
    Lang.initializeJDKLogging();
  }

  public static void traceLog() {
    Lang.initializeJDKLogging(MonitoringFactoryTest.class.getResource("/trace-log.properties"));
  }

}
