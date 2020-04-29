package com.remondis.limbus.engine.api;

import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

@LimbusBundle
@PublicComponent(requestType = SharedClasspathProvider.class, type = EmptySharedClasspathProvider.class)
public class EmptySharedClasspathBundle {

}
