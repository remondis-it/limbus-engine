package com.remondis.limbus.engine;

import com.remondis.limbus.engine.maintenance.LimbusMaintenanceConsole;
import com.remondis.limbus.engine.maintenance.LimbusMaintenanceConsoleImpl;
import com.remondis.limbus.engine.maintenance.LogFileViewer;
import com.remondis.limbus.engine.maintenance.ShowComponents;
import com.remondis.limbus.engine.maintenance.ShowDeployService;
import com.remondis.limbus.engine.maintenance.ShowEnvironment;
import com.remondis.limbus.engine.maintenance.ShowPluginClassPaths;
import com.remondis.limbus.engine.maintenance.ShowSharedClassPath;
import com.remondis.limbus.engine.maintenance.ShowTasks;
import com.remondis.limbus.engine.maintenance.ShowVersions;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PrivateComponent;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Activates the {@link LimbusMaintenanceConsole} with predefined features.
 */
@LimbusBundle
@PublicComponent(requestType = LimbusMaintenanceConsole.class, type = LimbusMaintenanceConsoleImpl.class)
@PrivateComponent(ShowEnvironment.class)
@PrivateComponent(ShowComponents.class)
@PrivateComponent(ShowTasks.class)
@PrivateComponent(ShowVersions.class)
@PrivateComponent(LogFileViewer.class)
@PrivateComponent(ShowSharedClassPath.class)
@PrivateComponent(ShowPluginClassPaths.class)
@PrivateComponent(ShowDeployService.class)
public class MaintenanceBundle {

}
