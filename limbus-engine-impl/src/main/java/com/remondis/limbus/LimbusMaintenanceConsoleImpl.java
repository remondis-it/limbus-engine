package com.remondis.limbus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;
import com.remondis.limbus.launcher.EngineUtil;
import com.remondis.limbus.maintenance.Action;
import com.remondis.limbus.maintenance.Category;
import com.remondis.limbus.maintenance.Item;
import com.remondis.limbus.utils.Lang;

public class LimbusMaintenanceConsoleImpl extends Initializable<Exception> implements LimbusMaintenanceConsole {

  private static final Logger log = LoggerFactory.getLogger(LimbusMaintenanceConsoleImpl.class);

  public static final String MAINTENANCE_CONSOLE_OPERATION_THREAD_NAME = "Maintenance Console Operation";

  public static final String MAINTENANCE_CONSOLE_THREAD_NAME = "Maintenance Console";

  private static final String VERSION_FORMAT = "Version: %s";
  private static final String CATEGORIES = "Categories";
  private static final String APPLICATION_TITLE = "Limbus Container - Maintenance Console";
  private static final String EXIT_TITLE = (char) 0x00AB + " Back";
  private static final String ROOT_TITLE = "Select Category...";
  protected Stack<Category> navigationStack;
  protected Category rootCategory;
  private Label breadcrumBar;
  private ActionListBox actions;

  private Object lock = new Object();

  private Runnable navigationAction = new Runnable() {

    @Override
    public void run() {
      select(actions.getSelectedIndex());
    }
  };
  private Runnable exitAction = new Runnable() {

    @Override
    public void run() {
      exit();
    }
  };

  private Component mainPanelComponent;
  private Panel mainPanel;
  private Component mainComponent;

  protected BasicWindow window;
  protected MultiWindowTextGUI gui;
  private Terminal terminal;
  private TerminalScreen screen;

  private Thread consoleThread;

  private Action currentAction;

  public LimbusMaintenanceConsoleImpl() {
    // Only object creation is done in lanterna here. No GUI Thread needed.
    LimbusMaintenanceConsoleImpl.this.window = new BasicWindow(APPLICATION_TITLE);
  }

  @Override
  public void updateCurrentPage() {
    gui.getGUIThread()
        .invokeLater(new Runnable() {

          @Override
          public void run() {
            synchronized (lock) {
              if (currentAction != null) {
                setPage(currentAction);
              }
            }
          }
        });
  }

  public void select(int index) {
    gui.getGUIThread()
        .invokeLater(new Runnable() {

          @Override
          public void run() {
            synchronized (lock) {
              Object select = navigationStack.peek()
                  .select(index);
              if (select instanceof Action) {
                Action action = (Action) select;
                currentAction = action;
                setPage(action);
              } else if (select instanceof Category) {
                navigationStack.push((Category) select);
                setCurrentCategory();
              }
            }
          }

        });
  }

  private void setPage(Action action) {
    Container component = action.getComponent(LimbusMaintenanceConsoleImpl.this);
    setContentPanel(action.getTitle(), component);
  }

  public void setCurrentCategory() {
    gui.getGUIThread()
        .invokeLater(new Runnable() {

          @Override
          public void run() {
            synchronized (lock) {
              Category category = navigationStack.peek();
              List<Item> items = category.getItems();

              actions.clearItems();
              for (Item item : items) {
                actions.addItem(item.getTitle(), navigationAction);
              }
              if (!isCurrentRoot()) {
                actions.addItem(EXIT_TITLE, exitAction);
              }
              actions.setSelectedIndex(0);
              updateBreadCrumbBar();
            }
          }
        });
  }

  private void updateBreadCrumbBar() {
    synchronized (lock) {
      StringBuilder path = new StringBuilder();

      if (isCurrentRoot()) {
        path.append(ROOT_TITLE);
      } else {
        path.append("View: ");
        Iterator<Category> it = this.navigationStack.iterator();
        while (it.hasNext()) {
          Category cat = it.next();
          if (isRoot(cat)) {
            continue;
          }
          path.append(cat.getTitle());
          if (it.hasNext()) {
            path.append(" ")
                .append((char) 0x00BB)
                .append(" ");
          }
        }
      }

      breadcrumBar.setText(path.toString());
    }
  }

  private boolean isCurrentRoot() {
    synchronized (lock) {
      return isRoot(navigationStack.peek());
    }
  }

  private boolean isRoot(Category category) {
    return category == rootCategory;
  }

  private void setContentPanel(String title, Component component) {
    window.setComponent(null);
    try {
      if (mainComponent != null) {
        mainPanel.removeComponent(mainComponent);
      }
      Component toAdd = component.withBorder(Borders.singleLine(title));
      mainPanel.addComponent(toAdd, BorderLayout.Location.CENTER);
      mainComponent = toAdd;
    } finally {
      window.setComponent(mainPanelComponent);
    }
  }

  @Override
  public void showMessagePanel(String message) {
    synchronized (lock) {
      gui.getGUIThread()
          .invokeLater(new Runnable() {

            @Override
            public void run() {
              Panel exceptionPanel = new Panel();
              exceptionPanel.setLayoutManager(new BorderLayout());
              TextBox errorText = new TextBox();
              errorText.setReadOnly(true);
              errorText.setText(message);
              exceptionPanel.addComponent(errorText, BorderLayout.Location.CENTER);
              setContentPanel("Exception", errorText);
            }
          });
    }
  }

  @Override
  public void showExceptionPanel(Exception e) {
    synchronized (lock) {
      gui.getGUIThread()
          .invokeLater(new Runnable() {

            @Override
            public void run() {
              Panel exceptionPanel = new Panel();
              exceptionPanel.setLayoutManager(new BorderLayout());
              TextBox errorText = new TextBox();
              errorText.setReadOnly(true);
              errorText.setText(Lang.exceptionAsString(e));
              exceptionPanel.addComponent(errorText, BorderLayout.Location.CENTER);
              setContentPanel("Exception", errorText);
            }
          });

    }
  }

  public void exit() {
    gui.getGUIThread()
        .invokeLater(new Runnable() {

          @Override
          public void run() {
            synchronized (lock) {
              if (navigationStack.peek() != rootCategory) {
                navigationStack.pop();
              }

              setCurrentCategory();
            }
          }
        });
  }

  private void showWelcomePanel() {
    Panel welcomePanel = new Panel();
    welcomePanel.setLayoutManager(new BorderLayout());
    TextBox welcomeText = new TextBox();
    welcomeText.setReadOnly(true);
    welcomeText.addLine("    ___       ___  _____ ______   ________  ___  ___  ________");
    welcomeText.addLine("    |\\  \\     |\\  \\|\\   _ \\  _   \\|\\   __  \\|\\  \\|\\  \\|\\   ____\\");
    welcomeText.addLine("    \\ \\  \\    \\ \\  \\ \\  \\\\\\__\\ \\  \\ \\  \\|\\ /\\ \\  \\\\\\  \\ \\  \\___|");
    welcomeText.addLine("     \\ \\  \\    \\ \\  \\ \\  \\\\|__| \\  \\ \\   __  \\ \\  \\\\\\  \\ \\_____  \\");
    welcomeText.addLine("      \\ \\  \\____\\ \\  \\ \\  \\    \\ \\  \\ \\  \\|\\  \\ \\  \\\\\\  \\|____|\\  \\");
    welcomeText.addLine("       \\ \\_______\\ \\__\\ \\__\\    \\ \\__\\ \\_______\\ \\_______\\____\\_\\  \\");
    welcomeText.addLine("        \\|_______|\\|__|\\|__|     \\|__|\\|_______|\\|_______|\\_________\\");
    welcomeText.addLine("                                                         \\|_________|");
    welcomeText.addLine("");
    welcomeText.addLine("             _______   ________   ________  ___  ________   _______   ");
    welcomeText.addLine("            |\\  ___ \\ |\\   ___  \\|\\   ____\\|\\  \\|\\   ___  \\|\\  ___ \\  ");
    welcomeText.addLine("            \\ \\   __/|\\ \\  \\\\ \\  \\ \\  \\___|\\ \\  \\ \\  \\\\ \\  \\ \\   __/| ");
    welcomeText.addLine("             \\ \\  \\_|/_\\ \\  \\\\ \\  \\ \\  \\  __\\ \\  \\ \\  \\\\ \\  \\ \\  \\_|/__");
    welcomeText
        .addLine("              \\ \\  \\_|\\ \\ \\  \\\\ \\  \\ \\  \\|\\  \\ \\  \\ \\  \\\\ \\  \\ \\  \\_|\\ \\");
    welcomeText.addLine("               \\ \\_______\\ \\__\\\\ \\__\\ \\_______\\ \\__\\ \\__\\\\ \\__\\ \\_______\\");
    welcomeText.addLine("                \\|_______|\\|__| \\|__|\\|_______|\\|__|\\|__| \\|__|\\|_______|");
    welcomeText.addLine("");
    welcomeText.addLine("Welcome to Limbus Engine - a hot-deploy classloading container.");
    welcomeText.addLine("");
    welcomeText.addLine("Use the navigation bar on the left to select the available maintenance pages.");
    welcomeText.addLine("");
    welcomeText.addLine("The currently running Limbus Engine version is: " + EngineUtil.getEngineVersion());
    welcomePanel.addComponent(welcomeText, BorderLayout.Location.CENTER);
    setContentPanel("Welcome", welcomePanel);
    window.setFocusedInteractable(welcomeText);
  }

  /**
   * <p>
   * <b>Note: Operations on the GUI object must be done in the Lanterna GUI Thread!!</b>
   * </p>
   *
   * @return Returns the GUI of this {@link LimbusMaintenanceConsoleImpl}.
   */
  @Override
  public MultiWindowTextGUI getGui() {
    return gui;
  }

  @Override
  public void updateScreen() {
    gui.getGUIThread()
        .invokeLater(new Runnable() {

          @Override
          public void run() {
            try {
              LimbusMaintenanceConsoleImpl.this.gui.updateScreen();
            } catch (IOException e) {
              // Nothing to do here
            }
          }
        });

  }

  private void createRootCateogory() {
    synchronized (lock) {
      Category category = new Category(ROOT_TITLE);
      rootCategory = category;
      navigationStack.push(category);
    }
  }

  @Override
  public void addNavigationItems(Item... items) {
    synchronized (lock) {
      rootCategory.add(items);
      setCurrentCategory();
    }
  }

  @Override
  protected void performInitialize() throws Exception {
    // We have to wait until the console thread has reached addWindowAndWait. If we return earlier from this method, a
    // finish on the LimbusMaintenanceConsoleImpl can
    final Semaphore waitUntilInitialized = new Semaphore(1);
    waitUntilInitialized.acquire();

    final AtomicReference<Exception> caughtExceptionsFromLanterna = new AtomicReference<>();
    navigationStack = new Stack<>();
    createRootCateogory();

    this.consoleThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {

          // Setup terminal and screen layers

          DefaultTerminalFactory factory = new DefaultTerminalFactory();
          factory.setPreferTerminalEmulator(true);
          factory.setTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
          terminal = factory.createTerminal();
          screen = new TerminalScreen(terminal);
          screen.startScreen();

          gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

          // Create panel to hold components
          mainPanel = new Panel();
          mainPanel.setLayoutManager(new BorderLayout());

          // Bread Crumb Bar on top
          breadcrumBar = new Label(ROOT_TITLE);

          // Action panel on the left
          Panel actionPanel = null;
          {
            actionPanel = new Panel();
            actions = new ActionListBox();
            actionPanel.addComponent(actions);
          }

          mainPanel.addComponent(breadcrumBar, BorderLayout.Location.TOP);
          mainPanel.addComponent(actionPanel.withBorder(Borders.singleLine(CATEGORIES)), BorderLayout.Location.LEFT);
          Label versionLabel = new Label(String.format(VERSION_FORMAT, EngineUtil.getEngineVersion()));
          Panel versionPanel = new Panel(new BorderLayout());
          versionPanel.addComponent(versionLabel, BorderLayout.Location.RIGHT);
          mainPanel.addComponent(versionPanel, BorderLayout.Location.BOTTOM);

          mainPanelComponent = mainPanel.withBorder(Borders.singleLine(APPLICATION_TITLE));

          // Create window to hold the panel
          window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));
          window.setComponent(mainPanelComponent);

          setCurrentCategory();

          gui.updateScreen();

          showWelcomePanel();

          waitUntilInitialized.release();
          gui.addWindowAndWait(LimbusMaintenanceConsoleImpl.this.window);

        } catch (Exception e) {
          caughtExceptionsFromLanterna.set(e);
          e.printStackTrace();
        } finally {
          log.debug("Limbus Maintenance Console Thread stopped.");
          waitUntilInitialized.release();
        }
      }

    }, MAINTENANCE_CONSOLE_THREAD_NAME);

    consoleThread.start();

    // Throw exception from initialization
    throwConsoleExceptions(caughtExceptionsFromLanterna);

    waitUntilInitialized.acquireUninterruptibly();
    waitUntilInitialized.release();

  }

  private void throwConsoleExceptions(final AtomicReference<Exception> caughtExceptionsFromLanterna) throws Exception {
    // Throw any exceptions that occurred while initializing lanterna
    Exception exception = caughtExceptionsFromLanterna.get();
    if (exception != null) {
      throw exception;
    }
  }

  @Override
  protected void performFinish() {
    try {

      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          if (window != null) {
            try {
              window.close();
            } catch (Exception e) {
              // Keep this silent.
            }
          }

          if (screen != null) {
            try {
              screen.stopScreen();
            } catch (Exception e) {
              // Keep this silent.
            }
          }

        }
      });

    } catch (Exception e1) {
      log.warn("Error while stopping Limbus Maintenance Console.", e1);
    }

    try {
      consoleThread.join();
    } catch (InterruptedException e) {
    }
  }

}
