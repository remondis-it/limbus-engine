package com.remondis.limbus.staging;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.remondis.limbus.engine.api.LimbusEngine;

@RunWith(MockitoJUnitRunner.class)
public class LimbusStagingTest {

  @Mock
  private LimbusEngine limbusEngineMock;

  @BeforeClass
  public static void beforeClass() {
    LimbusStaging.prepareEnvironment();
  }

  @AfterClass
  public static void afterClass() {
    LimbusStaging.resetEnvironment();
  }

  @Test
  public void shouldStartLimbusStage() throws Exception {
    LimbusStage stage = LimbusStaging.fromComponents()
        .addPublicComponentMock(LimbusEngine.class, limbusEngineMock)
        .addComponentConfiguration(MockComponent.class, MockComponentImpl.class)
        .buildStage();

    try {
      stage.startStage();
      MockComponent mockComponent = stage.getComponent(MockComponent.class);
      assertEquals(MockComponentImpl.HELLO_WORLD, mockComponent.sayHello());
    } finally {
      stage.stopStage();
    }
  }

  @Test
  public void shouldStartLimbusStageFromApplication() throws Exception {
    LimbusStage stage = LimbusStaging.fromComponentsFromApplication(MockApplication.class)
        .addPublicComponentMock(LimbusEngine.class, limbusEngineMock)
        .buildStage();

    try {
      stage.startStage();
      MockComponent mockComponent = stage.getComponent(MockComponent.class);
      assertEquals(MockComponentImpl.HELLO_WORLD, mockComponent.sayHello());
    } finally {
      stage.stopStage();
    }
  }

}
