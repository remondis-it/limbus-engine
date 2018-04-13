package com.remondis.limbus_integrations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.remondis.limbus.Classpath;
import com.remondis.limbus.DeploymentListener;
import com.remondis.limbus.LimbusDefaultComponents;
import com.remondis.limbus.LimbusEngine;
import com.remondis.limbus.LimbusPlugin;
import com.remondis.limbus.UndeployVetoException;
import com.remondis.limbus.Veto;
import com.remondis.limbus.files.InMemoryFilesystemImpl;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.launcher.LimbusStage;
import com.remondis.limbus.launcher.LimbusStaging;

@RunWith(MockitoJUnitRunner.class)
public class LimbusIntegrationTest implements DeploymentListener {

  private static final String DEPLOY_NAME = "deployName";

  private LimbusStage stage;

  private LimbusEngine engine;

  private boolean undeployVeto = false;

  @BeforeClass
  public static void beforeClass() throws Exception {
    LimbusStaging.prepareEnvironment();
  }

  @AfterClass
  public static void afterClass() {
    LimbusStaging.resetEnvironment();
  }

  @Before
  public void before() throws Exception {

    InMemoryFilesystemImpl filesystem = new InMemoryFilesystemImpl();

    // @formatter:off
     this.stage = LimbusStaging.create(DEPLOY_NAME)
                 .andClasses(TestPlugin.class)
                 .withDefaultLimbusComponents(new LimbusDefaultComponents())
                 /*
                  *  schuettec - 18.04.2017 : Add the TestEngine, because the proprietary plugin interface
                  *  org.testclient.TestPlugin is used
                  */
                 .addComponentConfiguration(LimbusEngine.class, TestEngine.class)
                 .addPublicComponentMock(LimbusFileService.class, filesystem)
                 .buildStage();
    // @formatter:on
    stage.startStage();

    this.engine = stage.getComponent(LimbusEngine.class);
    engine.addDeploymentListener(this);
  }

  @Test
  public void test() throws Exception {
    // schuettec - 16.05.2017 : Deny undeploy!
    undeployVeto = true;

    Classpath classpath = engine.getClasspath(DEPLOY_NAME);
    LimbusPlugin plugin = engine.getPlugin(classpath, TestPlugin.class.getName(), LimbusPlugin.class);
    assertNotNull(plugin);

    try {
      engine.undeployPlugin(classpath);
      fail("Undeploy: Veto was expected!");
    } catch (UndeployVetoException e) {
      // Totally expected
    }

    undeployVeto = false;

    engine.undeployPlugin(classpath);
  }

  @Override
  public void classpathUndeploying(Classpath classpath, Veto veto) {
    if (undeployVeto) {
      veto.veto();
    }
  }

  @After
  public void after() {
    if (stage != null) {
      engine.removeDeploymentListener(this);
      stage.stopStage();
    }
  }

}
