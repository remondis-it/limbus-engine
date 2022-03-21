package com.remondis.limbus.engine.security;

import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.properties.LimbusProperties;
import com.remondis.limbus.utils.Lang;

/**
 * This is an implementation of the {@link SecurityManager} to provide a better logging to detect required permissions.
 * This implementation can be configured to log permissions requests and their results.
 *
 *
 * <p>
 * <b>Note: Do not alter the functionality of this {@link SecurityManager} because the Limbus Security relies heavily on
 * the default Java Access Controller to grant permissions to the right {@link ProtectionDomain}s. This decision depends
 * on the context of classloading and codesources and cannot be calculated in this class completely.</b>
 * </p>
 *
 * @author schuettec
 *
 */
public class LimbusSecurityManager extends SecurityManager {

  private static final RuntimePermission MODFIY_THREAD_PERMISSION = new RuntimePermission("modifyThreadGroup");

  /**
   * Holds the Limbus Security Manager properties.
   */
  private LimbusProperties properties;

  /**
   * Holds the context classloader type filter.
   */
  private Pattern filterPattern;

  public LimbusSecurityManager(LimbusFileService filesystem) {
    Lang.denyNull("filesystem", filesystem);
    try {
      this.properties = new LimbusProperties(filesystem, LimbusSecurityManager.class, true, false);
    } catch (Exception e) {
      throw new LimbusSecurityException(
          "Cannot initialize Limbus Security Manager due to missing default configuration.");
    }
    init();
  }

  private void init() {
    if (logRequests() && hasContextClassLoaderFilter()) {
      filterPattern = Pattern.compile(contextClassLoaderFilter());
    }
  }

  @Override
  public void checkAccess(ThreadGroup g) {
    super.checkAccess(g);
    checkPermission(MODFIY_THREAD_PERMISSION);
  }

  @Override
  public void checkPermission(Permission perm) {
    try {
      super.checkPermission(perm);
      logRequest(perm);
    } catch (SecurityException e) {
      logSecurityException(perm, e);
    }
  }

  private void logRequest(Permission perm) {
    if (logRequests()) {
      String request = getRequest();
      boolean handleRequest = handleRequest(request);
      if (handleRequest) {
        // schuettec - 06.02.2017 : ISSUE #69 : We have no other chance to reach the plugin's output here than using
        // System.out because we do not have a reference to the plugin's custom logger.
        System.out.printf("Permission granted %s.", perm.toString());
        if (dumpRequestThreadStack()) {
          dumpStackTrace();
        }
      }
    }
  }

  private void logSecurityException(Permission perm, SecurityException e) throws SecurityException {
    if (logDeny()) {
      // schuettec - 06.02.2017 : ISSUE #69 : We have no other chance to reach the plugin's output here than using
      // System.out because we do not have a reference to the plugin's custom logger.
      System.out.println("Permission was not granted: " + perm.toString());
      if (dumpDenyThreadStack()) {
        dumpStackTrace();
      }
    }
    throw e;
  }

  private void dumpStackTrace() {
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @Override
      public Void run() {
        StackTraceElement[] stackTraceElements = Thread.getAllStackTraces()
            .get(Thread.currentThread());
        String stackTraceAsString = Lang.stackTraceAsString(stackTraceElements);
        System.out.println("Logging stacktrace of the request:");
        System.out.println(stackTraceAsString);
        return null;
      }
    });
  }

  /**
   * @return Returns the request as string. If a context classloader is set on the current thread, the class name of it
   *         is returned.
   */
  private String getRequest() {
    ClassLoader contextClassLoader = Thread.currentThread()
        .getContextClassLoader();
    if (contextClassLoader == null) {
      return null;
    } else {
      return contextClassLoader.getClass()
          .getCanonicalName();
    }
  }

  /**
   * @return Returns <code>true</code> if the request is to handle because the context classloader filter matches or
   *         because no filter was specified. Returns <code>false</code> if there is a filter that does not match the
   *         request.
   */
  private boolean handleRequest(String request) {
    if (request == null) {
      return true;
    }
    boolean handleRequest = true;
    if (hasContextClassLoaderFilter()) {
      Matcher m = filterPattern.matcher(request);
      handleRequest = m.matches();
    }
    return handleRequest;
  }

  private boolean hasContextClassLoaderFilter() {
    return contextClassLoaderFilter() != null;
  }

  /**
   * The configuration key 'contextClassLoaderFilter' specifies a regexp which is used to filter the thread context
   * classloaders. Only matching context classloaders are logged on permission request.
   *
   * @return Returns the configuration value for key 'contextClassLoaderFilter'
   */
  private String contextClassLoaderFilter() {
    return properties.getProperty("contextClassLoaderFilter");
  }

  private boolean logRequests() {
    return properties.getBoolean("logRequests");
  }

  private boolean dumpDenyThreadStack() {
    return properties.getBoolean("dumpDenyThreadStack");
  }

  private boolean dumpRequestThreadStack() {
    return properties.getBoolean("dumpRequestThreadStack");
  }

  private boolean logDeny() {
    return properties.getBoolean("logDeny");
  }
}
