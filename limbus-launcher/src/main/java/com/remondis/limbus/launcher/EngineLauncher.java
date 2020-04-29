package com.remondis.limbus.launcher;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.activators.logging.LoggingActivator;
import com.remondis.limbus.activators.logging.LoggingActivatorException;
import com.remondis.limbus.activators.monitoring.MonitoringActivator;
import com.remondis.limbus.system.LimbusSystem;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * This is the main entry point for launching the runtime engine. For use as linux daemon there is a shutdown hook that
 * tries to finish the engine gracefully. This was done to get around more complex service wrappers etc. <b>There are
 * circumstances where the shutdown hook is not called.</b>
 * <p>
 * The known situations where the shutdown hook is not called are:
 * <ul>
 * <li>Running on a windows machine that shuts down</li>
 * <li>Running in eclipse and terminated using the red button</li>
 * <li>Running in Cygwin and terminated with CTRL^c</li>
 * </ul>
 * The shutdown hook is triggered when
 * <ul>
 * <li>Running in a windows CMD and terminated using CTRL^C</li>
 * <li>Running in a linux shell and terminated using CTRL^C</li>
 * <li>Running on a linux machine thats shuts down</li>
 * </ul>
 * Since we are able to detect the linux shutdown, and developers are able to terminate the engine safely using CTRL^C
 * this is a suitable solution for now. If the requirements become more complex, this can be done by a service wrapper
 * (but most of them are using native solutions which is :-( ).
 * </p>
 *
 * <p>
 * The application can be started using the default main method. The linux service script (engine.sh) shipped with this
 * module handles the daemon-start/stop logic on linux machines.
 * </p>
 *
 * <p>
 * <b>Logging frameworks use to install shutdown hooks and therefore will not work in the shutdown sequence. This is why
 * it was decided to use std/out and the logger for logging on {@link EngineLauncher} level.</b>
 * </p>
 *
 * @author schuettec
 *
 */
public class EngineLauncher {

  private static Logger log = null;

  private static final String ENGINE_SHUTDOWN = "Engine Shutdown";

  /**
   * Makes it possible to wait for the engine to shut down and protects the concurrent bootstrapping of engines.
   */
  private static Semaphore waitForSemaphore = new Semaphore(1);

  private static Semaphore semaphore = new Semaphore(1);

  private static AtomicBoolean shutdownSequenceRunning = new AtomicBoolean(false);

  private static Thread shutdownHook;
  private static Engine currentEngine;

  private static PrintStream originalSystemOut;
  private static PrintStream originalSystemErr;

  private static WrappedOutputStream wrappedSystemOut;
  private static WrappedOutputStream wrappedSystemErr;

  private static LoggingActivator loggingActivator;

  private static MonitoringActivator monitoringActivator;

  private static ThreadSnapshot threadSnapshot;

  /**
   * For test purposes, used to skip the real system exit for tests.
   *
   * <p>
   * <b>
   * Deprecation: Use this field only for integration tests. Always keep in mind, that static attributes are shared
   * between JUnit test cases that are not part of this test suite. Use of this field is therefore dangerous. If you set
   * a value, always remember the old value and restore it after performing your test.
   * </p>
   * </b>
   */
  public static boolean skipSystemExit = false;

  /**
   * For test purposes, holds the wasDirty flag of the last shutdown sequence.
   */
  public static Boolean lastShutdownWasDirty = new Boolean(false);

  public static void main(String[] args) {
    Options options = new Options();
    Option help = Option.builder("h")
        .longOpt("help")
        .desc("Print out information for command line usage.")
        .build();
    Option wait = Option.builder("w")
        .longOpt("wait")
        .desc(
            "Shows a prompt to confirm before bootstrapping. (Useful to connect with JVisualVM before the bootstrapping sets the SecurityManager)")
        .numberOfArgs(1)
        .optionalArg(true)
        .type(String.class)
        .valueSeparator()
        .build();
    Option explicitEngine = Option.builder("e")
        .longOpt("engine")
        .desc("Sets the explicit engine implementation. Note: Service Loader will be ignored.")
        .numberOfArgs(1)
        .optionalArg(true)
        .type(String.class)
        .valueSeparator()
        .build();

    options.addOption(help);
    options.addOption(wait);
    options.addOption(explicitEngine);

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);

      // Wait for monitoring before bootstrapping
      if (cmd.hasOption(wait.getOpt())) {
        System.out.println("Wait option was used - hit ENTER to bootstrap engine:");
        try (Scanner sc = new Scanner(System.in)) {
          sc.nextLine();
        }
      }

      Engine engine = null;

      // Engine option
      if (cmd.hasOption(explicitEngine.getOpt())) {
        String engineClassName = cmd.getOptionValue(explicitEngine.getOpt());
        engine = getEngineForName(engineClassName);
      } else {
        engine = getEngineFromServices();
      }

      // Bootstrapping
      bootstrap(engine);

    } catch (ParseException e) {
      System.out.println("Wrong arguments: " + e.getMessage());
      printUsageInformation(options);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static void redirectOnDemand() {
    if (originalSystemOut == null || originalSystemErr == null) {
      redirectSystemOuts();
    }
  }

  /**
   * Redirects the system output stream to the specified target.
   *
   * @param newTarget
   *        The new target stream.
   */
  public static void redirectSystemOut(OutputStream newTarget) {
    if (wrappedSystemOut == null) {
      throw new IllegalStateException("The engine was not bootstrapped.");
    } else {
      wrappedSystemOut.setDelegate(newTarget);
    }
  }

  /**
   * Redirects the system error stream to the specified target.
   *
   * @param newTarget
   *        The new target stream.
   */
  public static void redirectSystemError(OutputStream newTarget) {
    if (wrappedSystemErr == null) {
      throw new IllegalStateException("The engine was not bootstrapped.");
    } else {
      wrappedSystemErr.setDelegate(newTarget);
    }
  }

  public static PrintStream getOriginalSystemOut() {
    redirectOnDemand();
    return new PrintStream(new WrappedOutputStream(originalSystemOut), true);
  }

  public static PrintStream getOriginalSystemErr() {
    redirectOnDemand();
    return new PrintStream(new WrappedOutputStream(originalSystemErr), true);
  }

  public static void resetSystemOut() {
    if (originalSystemOut != null) {
      redirectSystemOut(originalSystemOut);
    }
  }

  public static void resetSystemError() {
    if (originalSystemErr != null) {
      redirectSystemError(originalSystemErr);
    }
  }

  private static void printUsageInformation(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Limbus Engine", options);
  }

  /**
   * Bootstraps a Limbus System engine that utilizes {@link LimbusSystem} to create a
   * runtime environment of components.
   *
   * @param system
   *        The uninitialized configured {@link LimbusSystem}
   * @throws Exception
   *         Thrown on any error while bootstrapping.
   */
  public static void bootstrapLimbusSystem(LimbusSystem system) throws Exception {
    SystemEngine engine = new SystemEngine(system);
    bootstrap(engine);
  }

  /**
   * Bootstraps a Limbus System engine that utilizes {@link LimbusSystem} and a valid system description to create a
   * runtime environment of components. The system descriptor can be provided in the following ways:
   * <ul>
   * <li>Place a file <tt>limbus-system.xml</tt> in the Limbus configuration directory.</li>
   * <li>Place a Limbus Property file for class {@link SystemEngine} overriding the default system descriptor filename.
   * </li>
   * </ul>
   *
   * @throws Exception
   *         Thrown on any bootstrapping error.
   */
  public static void bootstrapLimbusSystem() throws Exception {
    Engine engine;
    try {
      engine = getEngineByClass(SystemEngine.class);
    } catch (Exception e) {
      throw new Exception(
          String.format("Cannot get engine implementation using the specified class %s", SystemEngine.class.getName()),
          e);
    }
    bootstrap(engine);
  }

  /**
   * Bootstraps an engine implementation specified by its class.
   *
   * @param engineClass
   *        The engine class.
   * @throws Exception
   *         Thrown on any bootstrap error.
   */
  public static void bootstrap(Class<? extends Engine> engineClass) throws Exception {
    Engine engine;
    try {
      engine = getEngineByClass(engineClass);
    } catch (Exception e) {
      throw new Exception(
          String.format("Cannot get engine implementation using the specified class %s", engineClass.getName()), e);
    }
    bootstrap(engine);
  }

  /**
   * Bootstraps an engine instance.
   *
   * @param engine
   *        The engine to bootstrap.
   * @throws Exception
   *         Thrown on any error that prevents the bootstrap to be performed. Runtime exceptions are not necessary to
   *         be handled.
   */
  public static void bootstrap(Engine engine) throws Exception {
    boolean acquired = waitForSemaphore.tryAcquire();
    if (!acquired) {
      throw new EngineLaunchException("Cannot bootstrap engine because an engine lifecycle is already running.");
    }

    try {
      semaphore.acquire();

      currentEngine = engine;

      threadSnapshot = ThreadSnapshot.snapshot();

      redirectSystemOuts();

      createLoggingEnvironment();

      createMonitoringEnvironment();

      try {

        installShutdownHook();

        // schuettec - 28.09.2016 : Do not use Lang.initializeLogging() here, because we want to be independent of any
        // concrete logging framework

        log.info("Initializing engine.");

        log.info("Engine instance successfully initialized.");

        log.info("Starting engine.");
        // notify the start event
        engine.startEngine();

      } catch (Exception e) {
        // Log initialization exception
        log.error("Cannot start engine due to exception on initialisation.", e);
        internalShutdown();
        throw e;
      }
    } catch (Exception e) {
      // Release the waitForSemaphore lock
      waitForSemaphore.release();
      throw e;
    } finally {
      // Unlock
      semaphore.release();
    }

  }

  /**
   * This method tries to initialize the Limbus Monitoring Facade using the registered {@link MonitoringActivator}. If
   * non is installed the Limbus Monitoring Facade defaults to a no-op.
   */
  private static void createMonitoringEnvironment() {
    // Load logging activator
    ServiceLoader<MonitoringActivator> activatorLoader = ServiceLoader.load(MonitoringActivator.class);
    Iterator<MonitoringActivator> it = activatorLoader.iterator();
    if (it.hasNext()) {
      MonitoringActivator activator = it.next();
      monitoringActivator = activator;
      // Initialize logging environment
      monitoringActivator.initializeMonitoring();
      log.info("Monitoring activator loaded successfully - using {}", monitoringActivator.getClass()
          .getName());
    } else {
      log.info("No monitoring activator was registered - Limbus Monitoring defaults to no-op.");
    }

    if (it.hasNext()) {
      log.info("Multiple monitoring activators were registered - using {}", monitoringActivator.getClass()
          .getName());
    }
  }

  private static void createLoggingEnvironment() throws LoggingActivatorException {
    // Load logging activator
    ServiceLoader<LoggingActivator> activatorLoader = ServiceLoader.load(LoggingActivator.class);
    Iterator<LoggingActivator> it = activatorLoader.iterator();
    if (it.hasNext()) {
      LoggingActivator activator = it.next();
      loggingActivator = activator;
    } else {
      throw new LoggingActivatorException(
          String.format("No logging activator was registered. Use META-INF/services/%s to configure one.",
              LoggingActivator.class.getName()));
    }

    // Initialize logging environment
    loggingActivator.initialize();

    // Get the engine launcher logging
    log = LoggerFactory.getLogger(EngineLauncher.class);

    log.info("Logging activators loaded successfully - using {}", loggingActivator.getClass()
        .getName());
    if (it.hasNext()) {
      log.info("Multiple logging activators were registered - using {}", loggingActivator.getClass()
          .getName());
    }

  }

  private static void finishMonitoringEnvironment() {
    // Finish logging environment
    if (monitoringActivator != null) {
      monitoringActivator.finishMonitoring();
    }

    // Null out logging activator reference
    monitoringActivator = null;

  }

  private static void finishLoggingEnvironment() {
    // Finish logging environment
    if (loggingActivator != null) {
      loggingActivator.finish();
    }

    // Null out logging activator reference
    loggingActivator = null;

    // Null out logger
    log = null;
  }

  private static void redirectSystemOuts() {
    originalSystemOut = System.out;
    originalSystemErr = System.err;

    wrappedSystemOut = new WrappedOutputStream(originalSystemOut);
    wrappedSystemErr = new WrappedOutputStream(originalSystemErr);

    System.setOut(new PrintStream(wrappedSystemOut));
    System.setErr(new PrintStream(wrappedSystemErr));
  }

  private static void resetSystemOuts() {
    if (originalSystemOut == null || originalSystemErr == null) {
      return;
    }
    System.setOut(originalSystemOut);
    System.setErr(originalSystemErr);

    wrappedSystemOut.setDelegate(originalSystemOut);
    wrappedSystemOut = null;
    wrappedSystemErr.setDelegate(originalSystemErr);
    wrappedSystemErr = null;
  }

  private static void installShutdownHook() {
    // Install shutdown hook
    shutdownHook = new Thread(new Runnable() {
      @Override
      public void run() {
        internalShutdown();
      }
    }, ENGINE_SHUTDOWN);
    Runtime.getRuntime()
        .addShutdownHook(shutdownHook);
  }

  /**
   * Waits for the engine to shutdown.
   * <p>
   * <b>Important:</b> This method is deprecated because it is not recommended for use in productive environment. Most
   * of the time this runtime environment will be controlled by start/stop scripts of the OS that rely on the runtime to
   * not terminate without being requested to do so. For server environments it is not suitable to terminate.</br>
   * The access to this class will be denied for client objects in the future.
   * </p>
   */
  public static void waitForShutdown() {
    waitForSemaphore.acquireUninterruptibly();
    waitForSemaphore.release();
  }

  /**
   * Returns the running engine. This method is used for tests only. <b>Calling this method in a production environment
   * should ring your bells!</b>
   *
   * <p>
   * <b>Note: This method only returns the engine instance if the bootstrapping was successful.</b>
   * </p>
   *
   * <p>
   * <b>Note:</b> This method is not recommended for use in productive environment.
   * Plugins or other objects hosted by the engine should not be able to get control over it. The access to this class
   * will be denied for plugin objects in the future.
   * </p>
   *
   * @return Returns the bootstrapped engine or <code>null</code> if the engine didn't start correctly.
   */
  public static Engine getEngine() {
    try {
      // Wait for the engine to finish bootstrapping.
      semaphore.acquire();
      return currentEngine;
    } catch (InterruptedException e) {
      // On interrupt return no engine, because we may be currently bootstrapping
      return null;
    } finally {
      semaphore.release();
    }

  }

  /**
   * This method initiates the shutdown sequence.
   *
   * <p>
   * <b>Important:</b> This method is deprecated because it is not recommended for use in productive environment. Most
   * of the time this runtime environment will be controlled by start/stop scripts of the OS that rely on the runtime to
   * not terminate without being requested to do so. For server environments it is not suitable to terminate.</br>
   * The access to this class will be denied for client objects in the future.
   * </p>
   */
  public static void shutdownEngine() {
    // The shutdown hook is null if the engine was not bootstrapped.
    if (shutdownHook != null) {
      try {
        shutdownHook.start();
      } catch (Exception e) {
        /*
         * Keep this silent. The only thing that can happen here is that the shutdown
         * hook was started before (via Runtime shutdown hook signal). This results in an IllegalThreadState
         * exception saying us, that the shutdown was already triggered by the JVM runtime. In this case we can keep it
         * silent here because the shutdown is performed.
         */
      }
    }
  }

  private static void internalShutdown() {
    if (shutdownSequenceRunning.compareAndSet(false, true)) {

      boolean wasDirty = false;

      // If current thread is not shutdown hook, remove shutdown hook to avoid triggering shutdown sequence multiple
      // times.
      if (shutdownHook != null && Thread.currentThread() != shutdownHook) {
        Runtime.getRuntime()
            .removeShutdownHook(shutdownHook);
        // Reset shutdown hook
        shutdownHook = null;
      }

      try {
        log.info("Received shutdown signal - starting shutdown sequence.");
        // Try to stop the engine silently.
        // At this point the engine may be null, because the bootstrap may has failed.
        try {
          if (currentEngine != null) {
            log.info("Stop signal send to engine.");
            currentEngine.stopEngine();
            log.info("Engine stopped.");
            currentEngine = null;
          }
        } catch (Exception e) {
          log.error("Exception while stopping engine. This is an implementation fault.", e);
        }

        // Finish Monitoring environment
        finishMonitoringEnvironment();

        // Best effort clean up
        wasDirty = ThreadCleaning.finishRunningThreads(threadSnapshot, Thread.currentThread());
        if (wasDirty) {
          log.info("Engine didn't stopped cleanly - forcing termination.");
        } else {
          log.info("Engine stopped successfully.");
        }
        log.info("Bye.");

        // For test purposes
        lastShutdownWasDirty = wasDirty;

        // Finish Logging environment
        finishLoggingEnvironment();

      } catch (Throwable t) {
        // Nothing to do here.
        t.printStackTrace();
      } finally {
        // Reset system outs
        resetSystemOuts();

        shutdownSequenceRunning.set(false);

        waitForSemaphore.release();

        // Force exit if
        if (wasDirty) {
          if (!skipSystemExit) {
            if (Thread.currentThread() != shutdownHook) {
              System.exit(0);
            }
          }
        }
      }
    }
  }

  private static Engine getEngineForName(String engineClass) throws Exception {
    try {
      Class<Engine> engineCls = ReflectionUtil.loadServiceClass(engineClass, Engine.class);
      return getEngineByClass(engineCls);
    } catch (Exception e) {
      throw new Exception(String.format("Engine instance %s cannot be created.", engineClass), e);
    }
  }

  private static Engine getEngineByClass(Class<? extends Engine> engine)
      throws InstantiationException, IllegalAccessException {
    return engine.newInstance();
  }

  private static Engine getEngineFromServices() throws Exception {
    ServiceLoader<Engine> engineLoder = ServiceLoader.load(Engine.class);
    Iterator<Engine> it = engineLoder.iterator();
    while (it.hasNext()) {
      Engine selectedEngine = it.next();
      if (it.hasNext()) {
        System.err.println("Multiple engine classes were defined in META-INF/services/" + Engine.class.getName());
        System.err.println(String.format("Selecting the first configured engine: %s", selectedEngine.getClass()
            .getName()));
      }
      return selectedEngine;
    }
    // No registered services
    throw new Exception("No engine class was defined in META-INF/services/" + Engine.class.getName());
  }

}
