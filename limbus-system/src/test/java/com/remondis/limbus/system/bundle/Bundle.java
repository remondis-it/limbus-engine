package com.remondis.limbus.system.bundle;

import com.remondis.limbus.system.LimbusBundle;
import com.remondis.limbus.system.PrivateComponent;
import com.remondis.limbus.system.PublicComponent;

@LimbusBundle
@PrivateComponent(BundlePrivateComponent.class)
@PublicComponent(requestType = BundlePublicComponent.class, type = BundlePublicComponentImpl.class)
public class Bundle {

}
