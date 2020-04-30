package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.security.LimbusSecurity;
import com.remondis.limbus.engine.security.LimbusSecurityImpl;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Bundle that exposes the {@link LimbusSecurity}.
 */
@LimbusBundle
@PublicComponent(requestType = LimbusSecurity.class, type = LimbusSecurityImpl.class)
public class LimbusSecurityBundle {

}
