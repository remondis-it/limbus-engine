package com.remondis.limbus.engine;

import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Activates the Limbus Deploy Service for the shared classpath.
 */
@LimbusBundle
@PublicComponent(requestType = SharedDeployService.class, type = SharedDeployServiceImpl.class)
public class SharedDeployServiceBundle {

}
