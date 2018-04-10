package org.max5.limbus.monitoring;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

public class MonitoringFactoryTest {

  @Test
  public void test_existing_systemproperty_config() {
    try {
      URL resource = MonitoringFactoryTest.class.getResource("/test_config.xml");
      System.setProperty(Conventions.PROPERTY_CONFIG_URL, resource.toString());
      MonitoringFactory.getMonitoring(getClass());
      assertEquals(InitState.INITIALIZED, MonitoringFactory.getState());
    } finally {
      System.getProperties()
          .remove(Conventions.PROPERTY_CONFIG_URL);
      MonitoringFactory.shutdown();
    }
  }

  @Test
  public void test_non_existing_systemproperty_config() {
    try {
      TestUtils.silentLog();
      System.setProperty(Conventions.PROPERTY_CONFIG_URL, "file:/WILL/NOT/EXISTS/test_config.xml");
      MonitoringFactory.getMonitoring(getClass());
      assertEquals(InitState.NOOP, MonitoringFactory.getState());
    } finally {
      // Reset to default log config
      TestUtils.defaultLog();
      System.getProperties()
          .remove(Conventions.PROPERTY_CONFIG_URL);
      MonitoringFactory.shutdown();
    }
  }

  @Test
  public void test() {
    try {
      MonitoringFactory.getMonitoring(getClass());
      assertEquals(InitState.INITIALIZED, MonitoringFactory.getState());
    } finally {
      MonitoringFactory.shutdown();
    }
  }

}
