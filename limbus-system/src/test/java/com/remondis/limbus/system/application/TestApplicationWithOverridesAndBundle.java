package com.remondis.limbus.system.application;

import com.remondis.limbus.system.ImportBundle;
import com.remondis.limbus.system.ReflectiveObjectFactory;
import com.remondis.limbus.system.api.LimbusApplication;
import com.remondis.limbus.system.api.PrivateComponent;
import com.remondis.limbus.system.api.PublicComponent;
import com.remondis.limbus.system.applicationExtern.ExternalPrivateComponent;
import com.remondis.limbus.system.applicationExtern.MyLocalPublicComponentImpl;
import com.remondis.limbus.system.bundle.Bundle;

@LimbusApplication(objectFactory = ReflectiveObjectFactory.class)
@PublicComponent(requestType = LocalPublicComponent.class, type = MyLocalPublicComponentImpl.class)
@PublicComponent(requestType = AnotherPublicComponent.class, type = AnotherPublicComponentImpl.class)
@PrivateComponent(ExternalPrivateComponent.class)
@ImportBundle(Bundle.class)
public class TestApplicationWithOverridesAndBundle {

}
