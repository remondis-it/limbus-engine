package com.remondis.limbus_integrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.LimbusPlugin;
import com.remondis.limbus.engine.LimbusDefaultComponents;
import com.remondis.limbus.engine.api.DeploymentListener;
import com.remondis.limbus.engine.api.InvocationResult;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.engine.api.UndeployVetoException;
import com.remondis.limbus.engine.api.Veto;
import com.remondis.limbus.files.InMemoryFilesystemImpl;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.staging.LimbusStage;
import com.remondis.limbus.staging.LimbusStaging;
import com.remondis.limbus.staging.LimbusStagingDeployment;

/**
 * Java 9 and later compatibility:
 * Limbus Engine performs some cleaning/classloader-leak prevention using reflective access to java.base/java.lang.
 * Therefore to execute the test you have to add
 * <tt>--add-opens java.base/java.lang=com.remondis.limbus.engine.implementation</tt>.
 * 
 * @author cschu
 *
 */
@ExtendWith(MockitoExtension.class)
public class LimbusIntegrationTest implements DeploymentListener {

  private static final String DEPLOY_NAME = "deployName";

  private LimbusStage stage;

  private LimbusEngine engine;

  private boolean undeployVeto = false;

  @BeforeAll
  public static void beforeClass() throws Exception {
    LimbusStaging.prepareEnvironment();
  }

  @AfterAll
  public static void afterClass() {
    LimbusStaging.resetEnvironment();
  }

  @BeforeEach
  public void before() throws Exception {

    InMemoryFilesystemImpl filesystem = new InMemoryFilesystemImpl();

    // @formatter:off
     this.stage = LimbusStaging
                 .fromDefaultLimbusComponents(new LimbusDefaultComponents())
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

    LimbusStagingDeployment deployment = stage.createDeployment(DEPLOY_NAME)
        .andClasses(TestPlugin.class);
    stage.deploy(deployment);

    Classpath classpath = engine.getClasspath(DEPLOY_NAME);
    // Call anonymous method (anonymous: method is not defined in one of the plugins interfaces)
    String name = "anonymousMethod";
    Class[] parameterTypes = new Class[] {};
    Object[] parameters = null;
    InvocationResult result = engine.invokePluginMethodReflectively(classpath, TestPlugin.class.getName(),
        LimbusPlugin.class, null, false, name, parameterTypes, parameters);
    assertNotNull(result);
    assertTrue(result.methodReturnsValues());
    Object returnValue = result.getReturnValue();
    assertNotNull(returnValue);
    assertThat(returnValue).isInstanceOf(String.class);
    assertFalse(((String) returnValue).isEmpty());

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

  @AfterEach
  public void after() {
    if (stage != null) {
      engine.removeDeploymentListener(this);
      stage.stopStage();
    }
  }

}
