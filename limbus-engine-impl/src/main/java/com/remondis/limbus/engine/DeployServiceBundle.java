package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.DeployService;
import com.remondis.limbus.engine.api.maven.MavenArtifactService;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Activates the Limbus Deploy Service.
 *
 */
@LimbusBundle
@PublicComponent(requestType = DeployService.class, type = DeployServiceImpl.class)
@PublicComponent(requestType = MavenArtifactService.class, type = MavenArtifactServiceIntegrator.class)
public class DeployServiceBundle {

}
