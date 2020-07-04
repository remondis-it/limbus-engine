package com.remondis.limbus.showcase.launcher;

import com.remondis.limbus.engine.ActionsBundle;
import com.remondis.limbus.engine.DefaultPluginOutputBundle;
import com.remondis.limbus.engine.PluginDeployServiceBundle;
import com.remondis.limbus.engine.LimbusSecurityBundle;
import com.remondis.limbus.engine.MaintenanceBundle;
import com.remondis.limbus.engine.NoOpEngine;
import com.remondis.limbus.engine.api.EmptySharedClasspathBundle;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.files.bundles.FileSystemBundle;
import com.remondis.limbus.system.ImportBundle;
import com.remondis.limbus.system.ReflectiveObjectFactory;
import com.remondis.limbus.system.api.LimbusApplication;
import com.remondis.limbus.system.api.PublicComponent;
import com.remondis.limbus.tasks.TaskSchedulerBundle;

@LimbusApplication(objectFactory = ReflectiveObjectFactory.class)
@PublicComponent(requestType = LimbusEngine.class, type = NoOpEngine.class)
@ImportBundle({
    FileSystemBundle.class, LimbusSecurityBundle.class, EmptySharedClasspathBundle.class, MaintenanceBundle.class,
    ActionsBundle.class, PluginDeployServiceBundle.class, DefaultPluginOutputBundle.class, TaskSchedulerBundle.class
})
public class ShowcaseApplication {

}
