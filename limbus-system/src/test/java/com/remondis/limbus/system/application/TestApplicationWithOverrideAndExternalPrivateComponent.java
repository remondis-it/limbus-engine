package com.remondis.limbus.system.application;

import com.remondis.limbus.system.ReflectiveObjectFactory;
import com.remondis.limbus.system.api.LimbusApplication;
import com.remondis.limbus.system.api.PrivateComponent;
import com.remondis.limbus.system.api.PublicComponent;
import com.remondis.limbus.system.applicationExtern.ExternalPrivateComponent;
import com.remondis.limbus.system.applicationExtern.MyLocalPublicComponentImpl;

@LimbusApplication(objectFactory = ReflectiveObjectFactory.class)
@PublicComponent(requestType = LocalPublicComponent.class, type = MyLocalPublicComponentImpl.class)
@PrivateComponent(ExternalPrivateComponent.class)
public class TestApplicationWithOverrideAndExternalPrivateComponent {

}
