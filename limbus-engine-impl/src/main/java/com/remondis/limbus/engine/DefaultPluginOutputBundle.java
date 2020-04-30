package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.LogTarget;
import com.remondis.limbus.engine.logging.SystemOutLogTarget;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Activates the default {@link LogTarget} that redirects plugin output to {@link System#out}.
 */
@LimbusBundle
@PublicComponent(requestType = LogTarget.class, type = SystemOutLogTarget.class)
public class DefaultPluginOutputBundle {

}
