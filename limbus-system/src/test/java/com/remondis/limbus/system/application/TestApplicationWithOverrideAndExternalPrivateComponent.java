package com.remondis.limbus.system.application;

import com.remondis.limbus.system.LimbusApplication;
import com.remondis.limbus.system.PrivateComponent;
import com.remondis.limbus.system.PublicComponent;
import com.remondis.limbus.system.applicationExtern.ExternalPrivateComponent;
import com.remondis.limbus.system.applicationExtern.PossibleLocalPublicComponentOverrideImpl;

@LimbusApplication
@PublicComponent(requestType = LocalPublicComponent.class, type = PossibleLocalPublicComponentOverrideImpl.class)
@PrivateComponent(ExternalPrivateComponent.class)
public class TestApplicationWithOverrideAndExternalPrivateComponent {

}
