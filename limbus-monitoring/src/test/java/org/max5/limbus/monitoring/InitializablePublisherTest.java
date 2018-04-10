package org.max5.limbus.monitoring;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.max5.limbus.monitoring.publisher.InitializablePublisher;
import org.max5.limbus.monitoring.publisher.InitializablePublisherImpl;

public class InitializablePublisherTest {

  /**
   * This test checks that the client context is determined correctly when using proxies.
   */
  @Test
  public void test_initializable_publisher() {
    InitializablePublisherImpl publisher = null;
    try {
      MonitoringFactory.configureMonitoring(getClass().getResource("/initializable_publisher_test.xml"));
      Set<Object> publishers = MonitoringFactory.getPublishers(getClass(), InitializablePublisher.class);
      assertEquals(1, publishers.size());
      Object p = publishers.iterator()
          .next();
      assertTrue(p instanceof InitializablePublisherImpl);
      publisher = (InitializablePublisherImpl) p;

    } finally {
      MonitoringFactory.shutdown();
    }

    // schuettec - 19.04.2017 : Do the asserts after shutting down the monitoring. Otherwise it cannot be guaranteed
    // that all monitoring records have been processed.
    assertEquals(1, publisher.init);
    assertEquals(1, publisher.finish);
  }

}
