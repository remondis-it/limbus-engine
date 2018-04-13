package com.remondis.limbus.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.remondis.limbus.monitoring.publisher.Runtime;
import com.remondis.limbus.monitoring.publisher.RuntimePublisherImpl;

public class RuntimePublisherTest {

  /**
   * This test checks that the client context is determined correctly when using proxies.
   */
  @Test
  public void test_clientContext_in_publisher() {
    RuntimePublisherImpl publisher = null;
    try {
      MonitoringFactory.configureMonitoring(getClass().getResource("/runtimePublisherTest.xml"));
      Monitoring m = MonitoringFactory.getMonitoring(getClass());
      m.publish(Runtime.class)
          .start();
      m.publish(Runtime.class)
          .stop();
      m.publish(Runtime.class)
          .publish();
      Set<Object> publishers = MonitoringFactory.getPublishers(getClass(), Runtime.class);
      assertEquals(1, publishers.size());
      Object p = publishers.iterator()
          .next();
      assertTrue(p instanceof RuntimePublisherImpl);
      publisher = (RuntimePublisherImpl) p;

    } finally {
      MonitoringFactory.shutdown();
    }

    // schuettec - 19.04.2017 : Do the asserts after shutting down the monitoring. Otherwise it cannot be guaranteed
    // that all monitoring records have been processed.
    Long duration = publisher.getDuration();
    assertNotNull(duration);
    assertEquals(getClass().getName(), publisher.getClassName());
    assertEquals("test_clientContext_in_publisher", publisher.getMethodName());

  }

}
