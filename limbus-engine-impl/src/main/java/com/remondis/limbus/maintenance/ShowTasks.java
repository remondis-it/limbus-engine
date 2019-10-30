package com.remondis.limbus.maintenance;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.remondis.limbus.engine.LimbusMaintenanceConsole;
import com.remondis.limbus.system.LimbusComponent;
import com.remondis.limbus.tasks.TaskInfo;
import com.remondis.limbus.tasks.TaskScheduler;

/**
 * This maintenance page shows scheduled periodic service tasks managed by {@link TaskScheduler}.
 *
 * @author schuettec
 *
 */
public class ShowTasks extends AbstractLimbusItem {

  @LimbusComponent
  private TaskScheduler scheduler;

  public ShowTasks() {
    super("Service tasks");
  }

  /*
   * (non-Javadoc)
   *
   * @see com.remondis.limbus.maintenance.Action#getComponent(com.remondis.limbus.LimbusMaintenanceConsole)
   */
  @Override
  public Container getComponent(final LimbusMaintenanceConsole console) {
    Panel main = new Panel(new LinearLayout(Direction.VERTICAL));

    Table<String> versions = new Table<String>("Last result", "Task name", "Period", "Rejected?");
    TableModel<String> tableModel = versions.getTableModel();

    List<TaskInfo> info = scheduler.getSchedulerInfo();
    for (TaskInfo i : info) {
      String lastSuccess = i.isLastSuccess() ? "SUCCESS" : "FAILURE";
      String waitTime = String.format("%dsec.", TimeUnit.MILLISECONDS.toSeconds(i.getCurrentWaitTime()));
      tableModel.addRow(lastSuccess, i.getTaskName(), waitTime, i.isRejected() ? "yes" : "no");

    }
    versions.addTo(main);
    main.addComponent(new EmptySpace());
    main.addComponent(new Button("Refresh", new Runnable() {

      @Override
      public void run() {
        console.updateCurrentPage();
      }
    }));
    return main;

  }

}
