package com.remondis.limbus.system.bundle;

import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PrivateComponent;
import com.remondis.limbus.system.api.PublicComponent;

@LimbusBundle
@PrivateComponent(BundlePrivateComponent.class)
@PublicComponent(requestType = BundlePublicComponent.class, type = BundlePublicComponentImpl.class)
public class Bundle {

}
