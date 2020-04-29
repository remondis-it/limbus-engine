package com.remondis.limbus.system.external;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.api.NotInitializedException;
import com.remondis.limbus.system.InfoRecord;
import com.remondis.limbus.system.LimbusCyclicException;
import com.remondis.limbus.system.LimbusSystem;
import com.remondis.limbus.system.LimbusSystemException;
import com.remondis.limbus.system.LimbusSystemListener;
import com.remondis.limbus.system.MockLimbusSystem;
import com.remondis.limbus.system.NoSuchComponentException;
import com.remondis.limbus.system.external.circular.CircularA;
import com.remondis.limbus.system.external.circular.CircularAImpl;
import com.remondis.limbus.system.external.circular.CircularB;
import com.remondis.limbus.system.external.circular.CircularBImpl;
import com.remondis.limbus.system.external.circular.CircularC;
import com.remondis.limbus.system.external.circular.CircularCImpl;
import com.remondis.limbus.system.external.circular.CircularD;
import com.remondis.limbus.system.external.circular.CircularDImpl;
import com.remondis.limbus.utils.SerializeException;

public class LimbusSystemTest {

  @Test
  public void test_LimbusSystemListener() throws Exception {
    A component = Mockito.mock(A.class,
        withSettings().extraInterfaces(LimbusSystemListener.class, IInitializable.class));

    MockLimbusSystem system = new MockLimbusSystem();
    system.addPublicComponentMock(A.class, component);

    system.initialize();

    InOrder order = inOrder(component);
    order.verify(component)
        .initialize();
    order.verify((LimbusSystemListener) component)
        .postInitialize();

    system.finish();

    order.verify((LimbusSystemListener) component)
        .preDestroy();
    order.verify(component)
        .finish();

  }

  @Test
  public void test_addMultiplePrivateComponents() throws LimbusSystemException, SerializeException {
    // schuettec - 22.02.2017 : This was a bug in earlier versions: If multiple private components were added, they were
    // not available due to the null key mapping.
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(A.class, true);
    system.addComponentConfiguration(B.class, true);
    system.initialize();

    A componentA = A.instance;
    B componentB = B.instance;
    assertNotNull(componentA);
    assertNotNull(componentB);
  }

  @Test
  public void test_addPrivateComponentMultipleTimes() throws LimbusSystemException, SerializeException {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(AggregatorImpl.class, true);
    system.addComponentConfiguration(ConsumerImpl.class, true);
    system.addComponentConfiguration(AggregatorImpl.class, true);
  }

  @Test
  public void test_addPublicComponentMultipleTimes() throws LimbusSystemException, SerializeException {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(Aggregator.class, AggregatorImpl.class);
    system.addComponentConfiguration(Consumer.class, ConsumerImpl.class);
    system.addComponentConfiguration(Filter.class, FilterImpl.class);
    system.addComponentConfiguration(Producer.class, ProducerImpl.class);
    system.addComponentConfiguration(Aggregator.class, AnotherAggregatorImpl.class);
    system.initialize();
    Aggregator component = system.getComponent(Aggregator.class);
    assertTrue(component instanceof AnotherAggregatorImpl);
  }

  @Test // Happy Path
  public void test_limbus_container_annotation() throws LimbusSystemException, SerializeException {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(Aggregator.class, AggregatorImpl.class);
    system.addComponentConfiguration(Consumer.class, ConsumerImpl.class);
    system.addComponentConfiguration(Filter.class, FilterImpl.class);
    system.addComponentConfiguration(Producer.class, ProducerImpl.class);
    system.addComponentConfiguration(OptionalComponent.class, false);
    system.initialize();

    Aggregator aggregator = system.getComponent(Aggregator.class);
    assertNotNull(aggregator);
    assertNotNull(system.getComponent(Consumer.class));
    assertNotNull(system.getComponent(Filter.class));
    assertNotNull(system.getComponent(Producer.class));

    AggregatorImpl aggregatorImpl = (AggregatorImpl) aggregator;
    assertNotNull(aggregatorImpl.getSystem());
  }

  @Test // Duplicate component configurations are allowed.
  public void test_config_with_duplicates() throws LimbusSystemException, SerializeException {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(Aggregator.class, AggregatorImpl.class);
    system.addComponentConfiguration(Consumer.class, ConsumerImpl.class);
    system.addComponentConfiguration(Aggregator.class, AnotherAggregatorImpl.class);
    system.addComponentConfiguration(Filter.class, FilterImpl.class);
    system.addComponentConfiguration(Producer.class, ProducerImpl.class);

    system.initialize();
    Aggregator component = system.getComponent(Aggregator.class);
    String message = component.getMessage();
    // The Filter will reverse the message
    String expected = new StringBuilder(ProducerImpl.MESSAGE).reverse()
        .toString();
    assertEquals(expected, message);

    assertNotNull(system.getComponent(Aggregator.class));
    assertNotNull(system.getComponent(Consumer.class));
    assertNotNull(system.getComponent(Filter.class));
    assertNotNull(system.getComponent(Producer.class));

    system.finish();
  }

  @Test(expected = LimbusCyclicException.class)
  public void test_circular_dependency_detection() throws LimbusSystemException {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(CircularD.class, CircularDImpl.class);
    system.addComponentConfiguration(CircularC.class, CircularCImpl.class);
    system.addComponentConfiguration(CircularB.class, CircularBImpl.class);
    system.addComponentConfiguration(CircularA.class, CircularAImpl.class);
    system.initialize();
  }

  @Test(expected = NoSuchComponentException.class)
  public void test_optional_failing_component() throws LimbusSystemException {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(Aggregator.class, AggregatorImpl.class);
    system.addComponentConfiguration(Consumer.class, ConsumerImpl.class);
    system.addComponentConfiguration(Filter.class, FilterImpl.class);
    system.addComponentConfiguration(Producer.class, ProducerImpl.class);
    system.addComponentConfiguration(OptionalComponent.class, false);
    system.initialize();

    assertNotNull(system.getComponent(Aggregator.class));
    assertNotNull(system.getComponent(Consumer.class));
    assertNotNull(system.getComponent(Filter.class));
    assertNotNull(system.getComponent(Producer.class));

    system.getComponent(OptionalComponent.class);

  }

  @Test
  public void test_failing_producer() {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(Aggregator.class, AggregatorImpl.class);
    system.addComponentConfiguration(Consumer.class, ConsumerImpl.class);
    system.addComponentConfiguration(Filter.class, FilterImpl.class);
    system.addComponentConfiguration(Producer.class, FailingProducerImpl.class);
    try {
      system.initialize();
      fail("LimbusSystemException was expected because FailingProducerImpl should fail on initializing.");
    } catch (LimbusSystemException e) {
      // Expected exception
    }
    try {
      system.getComponent(Aggregator.class);
      fail("NotInitializedException was expected because the Limbus System must have failed to start.");
    } catch (NotInitializedException e) {
      // Expected exception
    }
  }

  @Test // Happy Path
  public void test() throws LimbusSystemException {
    LimbusSystem system = new LimbusSystem();
    system.addComponentConfiguration(Aggregator.class, AggregatorImpl.class);
    system.addComponentConfiguration(Consumer.class, ConsumerImpl.class);
    system.addComponentConfiguration(Filter.class, FilterImpl.class);
    system.addComponentConfiguration(Producer.class, ProducerImpl.class);
    system.initialize();
    Aggregator component = system.getComponent(Aggregator.class);
    String message = component.getMessage();
    // The Filter will reverse the message
    String expected = new StringBuilder(ProducerImpl.MESSAGE).reverse()
        .toString();
    assertEquals(expected, message);

    assertNotNull(system.getComponent(Aggregator.class));
    assertNotNull(system.getComponent(Consumer.class));
    assertNotNull(system.getComponent(Filter.class));
    assertNotNull(system.getComponent(Producer.class));

    system.finish();

    List<InfoRecord> infoRecords = system.getInfoRecords();
    Class<?>[] initAndFinishOrder = new Class<?>[] {
        com.remondis.limbus.system.external.ProducerImpl.class, com.remondis.limbus.system.external.FilterImpl.class,
        com.remondis.limbus.system.external.ConsumerImpl.class,
        com.remondis.limbus.system.external.AggregatorImpl.class,
        com.remondis.limbus.system.external.AggregatorImpl.class,
        com.remondis.limbus.system.external.ConsumerImpl.class, com.remondis.limbus.system.external.FilterImpl.class,
        com.remondis.limbus.system.external.ProducerImpl.class
    };

    for (int i = 0; i < infoRecords.size(); i++) {
      assertEquals(initAndFinishOrder[i], infoRecords.get(i)
          .getComponent()
          .getConfiguration()
          .getComponentType());
    }

  }

}
