package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.DeployService;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Activates the Limbus Deploy Service.
 *
 */
@LimbusBundle
@PublicComponent(requestType = DeployService.class, type = DeployServiceImpl.class)
public class DeployServiceBundle {

}
