package com.remondis.limbus.engine;

import com.remondis.limbus.engine.actions.ActionService;
import com.remondis.limbus.engine.actions.LimbusActionService;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Exposes the {@link LimbusActionService}.
 */
@LimbusBundle
@PublicComponent(requestType = ActionService.class, type = LimbusActionService.class)
public class ActionsBundle {

}
