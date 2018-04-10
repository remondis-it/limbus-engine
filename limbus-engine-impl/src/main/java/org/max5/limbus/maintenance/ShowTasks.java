package org.max5.limbus.maintenance;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.max5.limbus.LimbusMaintenanceConsole;
import org.max5.limbus.system.LimbusComponent;
import org.max5.limbus.tasks.TaskInfo;
import org.max5.limbus.tasks.TaskScheduler;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

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
   * @see org.max5.limbus.maintenance.Action#getComponent(org.max5.limbus.LimbusMaintenanceConsole)
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
