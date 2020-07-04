package com.remondis.limbus.maintenance.console;

import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PrivateComponent;
import com.remondis.limbus.system.api.PublicComponent;

@LimbusBundle
@PublicComponent(requestType = LimbusMaintenanceConsole.class, type = LimbusMaintenanceConsoleImpl.class)
@PrivateComponent(LogFileViewer.class)
@PrivateComponent(ShowComponents.class)
@PrivateComponent(ShowDeployService.class)
@PrivateComponent(ShowEnvironment.class)
@PrivateComponent(ShowPluginClassPaths.class)
@PrivateComponent(ShowSharedClassPath.class)
@PrivateComponent(ShowTasks.class)
@PrivateComponent(ShowVersions.class)
public class LimbusMaintenanceConsoleBundle {

}
