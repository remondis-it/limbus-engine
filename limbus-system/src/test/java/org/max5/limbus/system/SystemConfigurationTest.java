package org.max5.limbus.system;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.max5.limbus.system.external.Aggregator;
import org.max5.limbus.system.external.AggregatorImpl;
import org.max5.limbus.system.external.AnotherAggregatorImpl;
import org.max5.limbus.system.external.OptionalComponent;

public class SystemConfigurationTest {

  @Test
  public void test() {

    SystemConfiguration sys = new SystemConfiguration();
    sys.addComponentConfiguration(new ComponentConfiguration(OptionalComponent.class, false));
    assertFalse(sys.containsRequestType(OptionalComponent.class));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfiguration(OptionalComponent.class, false)));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfiguration(OptionalComponent.class, true)));

    sys.removeComponentConfiguration(new ComponentConfiguration(OptionalComponent.class, true));
    assertFalse(sys.containsRequestType(OptionalComponent.class));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfiguration(OptionalComponent.class, false)));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfiguration(OptionalComponent.class, true)));

    sys.addComponentConfiguration(new ComponentConfiguration(Aggregator.class, AggregatorImpl.class));
    assertTrue(sys.containsRequestType(Aggregator.class));
    sys.removeComponentConfiguration(new ComponentConfiguration(Aggregator.class, null));
    assertFalse(sys.containsRequestType(Aggregator.class));

    sys.addComponentConfiguration(new ComponentConfiguration(Aggregator.class, AggregatorImpl.class));
    assertTrue(sys.containsRequestType(Aggregator.class));
    sys.removeByRequestType(Aggregator.class);
    assertFalse(sys.containsRequestType(Aggregator.class));

    sys.addComponentConfiguration(new ComponentConfiguration(AggregatorImpl.class, true));
    sys.addComponentConfiguration(new ComponentConfiguration(AnotherAggregatorImpl.class, true));

    assertFalse(sys.containsRequestType(AggregatorImpl.class));
    assertFalse(sys.containsRequestType(AnotherAggregatorImpl.class));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfiguration(AggregatorImpl.class, false)));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfiguration(AnotherAggregatorImpl.class, true)));
    sys.removeComponentConfiguration(new ComponentConfiguration(AggregatorImpl.class, true));
    sys.removeComponentConfiguration(new ComponentConfiguration(AnotherAggregatorImpl.class, true));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfiguration(AggregatorImpl.class, false)));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfiguration(AnotherAggregatorImpl.class, true)));

  }

}
