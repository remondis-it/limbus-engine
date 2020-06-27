package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.LogTarget;
import com.remondis.limbus.engine.api.SharedClasspathProvider;
import com.remondis.limbus.engine.logging.SystemOutLogTarget;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Provides the {@link SharedFileSystemClasspath}.
 */
@LimbusBundle
@PublicComponent(requestType = SharedClasspathProvider.class, type = SharedFileSystemClasspath.class)
public class SharedFileSystemClasspathProviderBundle {

}
