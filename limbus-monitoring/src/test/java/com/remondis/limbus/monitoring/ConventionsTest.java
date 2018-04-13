package com.remondis.limbus.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.remondis.limbus.monitoring.dummies.DummyPublisher;
import com.remondis.limbus.monitoring.dummies.DummyPublisherImpl;
import com.remondis.limbus.monitoring.dummies.DummyPublisherInheritImpl;
import com.remondis.limbus.monitoring.dummies.InvalidBecauseHasObjectReturnTypes;
import com.remondis.limbus.monitoring.dummies.InvalidBecauseIsClass;

public class ConventionsTest {

  @Test
  public void test_publisher_impl_convention() {
    Conventions.isValidPublisherImplementation(DummyPublisherImpl.class);
    Conventions.isValidPublisherImplementation(DummyPublisherInheritImpl.class);

    List<Class<?>> pubInts = Conventions.getPublisherInterfacesForImplementation(DummyPublisherImpl.class);
    assertEquals(1, pubInts.size());

    pubInts = Conventions.getPublisherInterfacesForImplementation(DummyPublisherInheritImpl.class);
    assertEquals(2, pubInts.size());

  }

  @Test
  public void test_publisher_convention() {
    assertTrue(Conventions.isValidPublisherInterface(DummyPublisher.class));
    assertFalse(Conventions.isValidPublisherInterface(InvalidBecauseIsClass.class));
    assertFalse(Conventions.isValidPublisherInterface(InvalidBecauseHasObjectReturnTypes.class));
    assertFalse(Conventions.isValidPublisherInterface(String.class));
  }

}
