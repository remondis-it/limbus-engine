package com.remondis.limbus.staging;

import com.remondis.limbus.system.ReflectiveObjectFactory;
import com.remondis.limbus.system.api.LimbusApplication;
import com.remondis.limbus.system.api.PublicComponent;

@LimbusApplication(objectFactory = ReflectiveObjectFactory.class)
@PublicComponent(requestType = MockComponent.class, type = MockComponentImpl.class)
public class MockApplication {

}
