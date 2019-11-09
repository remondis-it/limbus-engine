package com.remondis.limbus.system.application;

import com.remondis.limbus.system.ImportBundle;
import com.remondis.limbus.system.LimbusApplication;
import com.remondis.limbus.system.PrivateComponent;
import com.remondis.limbus.system.PublicComponent;
import com.remondis.limbus.system.applicationExtern.ExternalPrivateComponent;
import com.remondis.limbus.system.applicationExtern.PossibleLocalPublicComponentOverrideImpl;
import com.remondis.limbus.system.bundle.Bundle;

@LimbusApplication
@PublicComponent(requestType = LocalPublicComponent.class, type = PossibleLocalPublicComponentOverrideImpl.class)
@PublicComponent(requestType = AnotherPublicComponent.class, type = AnotherPublicComponentImpl.class)
@PrivateComponent(ExternalPrivateComponent.class)
@ImportBundle(Bundle.class)
public class TestApplicationWithOverridesAndBundle {

}
