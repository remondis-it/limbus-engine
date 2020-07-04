package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.PluginDeployService;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Activates the Limbus Deploy Service.
 *
 */
@LimbusBundle
@PublicComponent(requestType = PluginDeployService.class, type = PluginDeployServiceImpl.class)
public class PluginDeployServiceBundle {

}
