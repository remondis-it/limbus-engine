package com.remondis.limbus.tasks;

import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

@LimbusBundle
@PublicComponent(requestType = TaskScheduler.class, type = TaskSchedulerImpl.class)
public class TaskSchedulerBundle {

}
