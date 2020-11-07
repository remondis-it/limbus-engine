package com.remondis.limbus.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.remondis.limbus.system.external.Aggregator;
import com.remondis.limbus.system.external.AggregatorImpl;
import com.remondis.limbus.system.external.AnotherAggregatorImpl;
import com.remondis.limbus.system.external.OptionalComponent;

public class SystemConfigurationTest {

  @Test
  public void test() {

    SystemConfiguration sys = new SystemConfiguration();
    sys.addComponentConfiguration(new ComponentConfigurationImpl(OptionalComponent.class, false));
    assertFalse(sys.containsRequestType(OptionalComponent.class));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfigurationImpl(OptionalComponent.class, false)));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfigurationImpl(OptionalComponent.class, true)));

    sys.removeComponentConfiguration(new ComponentConfigurationImpl(OptionalComponent.class, true));
    assertFalse(sys.containsRequestType(OptionalComponent.class));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfigurationImpl(OptionalComponent.class, false)));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfigurationImpl(OptionalComponent.class, true)));

    sys.addComponentConfiguration(new ComponentConfigurationImpl(Aggregator.class, AggregatorImpl.class));
    assertTrue(sys.containsRequestType(Aggregator.class));
    sys.removeComponentConfiguration(new ComponentConfigurationImpl(Aggregator.class, null));
    assertFalse(sys.containsRequestType(Aggregator.class));

    sys.addComponentConfiguration(new ComponentConfigurationImpl(Aggregator.class, AggregatorImpl.class));
    assertTrue(sys.containsRequestType(Aggregator.class));
    sys.removeByRequestType(Aggregator.class);
    assertFalse(sys.containsRequestType(Aggregator.class));

    sys.addComponentConfiguration(new ComponentConfigurationImpl(AggregatorImpl.class, true));
    sys.addComponentConfiguration(new ComponentConfigurationImpl(AnotherAggregatorImpl.class, true));

    assertFalse(sys.containsRequestType(AggregatorImpl.class));
    assertFalse(sys.containsRequestType(AnotherAggregatorImpl.class));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfigurationImpl(AggregatorImpl.class, false)));
    assertTrue(sys.containsComponentConfiguration(new ComponentConfigurationImpl(AnotherAggregatorImpl.class, true)));
    sys.removeComponentConfiguration(new ComponentConfigurationImpl(AggregatorImpl.class, true));
    sys.removeComponentConfiguration(new ComponentConfigurationImpl(AnotherAggregatorImpl.class, true));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfigurationImpl(AggregatorImpl.class, false)));
    assertFalse(sys.containsComponentConfiguration(new ComponentConfigurationImpl(AnotherAggregatorImpl.class, true)));

  }

}
