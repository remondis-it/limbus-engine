package org.max5.limbus.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.max5.limbus.monitoring.publisher.ClientContextTestPublisher;
import org.max5.limbus.monitoring.publisher.ClientContextTestPublisherImpl;

public class ClientContextTest {

  /**
   * This test checks that the caller information are correct for the different constructors.
   */
  @Test
  public void test_caller_information() {
    ClientContext w = new ClientContext(Thread.currentThread(), 0);
    assertEquals(getClass().getName(), w.getClassName());
    assertEquals("test_caller_information", w.getMethodName());
  }

  /**
   * This test checks that the client context is determined correctly when using proxies.
   */
  @Test
  public void test_clientContext_in_publisher() {
    ClientContextTestPublisherImpl publisher = null;
    try {
      MonitoringFactory.configureMonitoring(getClass().getResource("/call_id_test.xml"));
      Monitoring m = MonitoringFactory.getMonitoring(getClass());
      m.publish(ClientContextTestPublisher.class)
          .saveClientContext();
      Set<Object> publishers = MonitoringFactory.getPublishers(getClass(), ClientContextTestPublisher.class);
      assertEquals(1, publishers.size());
      Object p = publishers.iterator()
          .next();
      assertTrue(p instanceof ClientContextTestPublisherImpl);
      publisher = (ClientContextTestPublisherImpl) p;

    } finally {
      MonitoringFactory.shutdown();
    }

    // schuettec - 19.04.2017 : Do the asserts after shutting down the monitoring. Otherwise it cannot be guaranteed
    // that all monitoring records have been processed.
    ClientContext clientContext = publisher.getAndClearClientContext();
    assertEquals(getClass().getName(), clientContext.getClassName());
    assertEquals("test_clientContext_in_publisher", clientContext.getMethodName());

  }

}
