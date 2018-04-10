package org.max5.limbus.monitoring;

import java.util.UUID;

import org.junit.Test;
import org.max5.limbus.monitoring.dummies.DummyPublisher;
import org.max5.limbus.monitoring.dummies.InvalidBecauseHasObjectReturnTypes;

public class ProxyUtilsTest {

  @Test
  public void test() {
    DummyPublisher noop = ProxyUtils.noopProxy(DummyPublisher.class);
    noop.publish(UUID.randomUUID()
        .toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_invalid() {
    ProxyUtils.noopProxy(InvalidBecauseHasObjectReturnTypes.class);
  }

}
