package com.remondis.limbus.engine;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.utils.Lang;

/**
 * This implementation of an {@link URLClassLoader} provides class loading access to a class path specified by a set of
 * URLs. This class loader follows the default delegation scheme, but provides an entry point used by the child first
 * {@link PluginClassLoader}.
 *
 * @author schuettec
 *
 */
public class SharedClassLoader extends PluginClassLoader {

  private static final Logger log = LoggerFactory.getLogger(SharedClassLoader.class);

  protected final List<String> allowedPackagePrefixes;

  public SharedClassLoader(LimbusFileService filesystem, ClassLoader parent, List<String> accessiblePackages,
      Set<URL> urls) {
    super(filesystem, parent, urls);
    Lang.denyNull("accessiblePackages", accessiblePackages);
    Lang.denyNull("urls", urls);
    deactivateCleaning();
    allowedPackagePrefixes = new ArrayList<String>(accessiblePackages);
  }

  public SharedClassLoader(LimbusFileService filesystem, ClassLoader parent, List<String> accessiblePackages,
      URL... urls) {
    super(filesystem, parent, urls);
    Lang.denyNull("accessiblePackages", accessiblePackages);
    Lang.denyNull("urls", urls);
    deactivateCleaning();
    allowedPackagePrefixes = new ArrayList<String>(accessiblePackages);

  }

  public SharedClassLoader(LimbusFileService filesystem, ClassLoader parent, List<String> accessiblePackages,
      URLStreamHandlerFactory factory, URL... urls) {
    super(filesystem, parent, factory, urls);
    Lang.denyNull("accessiblePackages", accessiblePackages);
    Lang.denyNull("urls", urls);
    deactivateCleaning();
    allowedPackagePrefixes = new ArrayList<String>(accessiblePackages);
  }

  @Override
  public URL getResource(String name) {

    URL url = null;

    // Child first lookup
    url = findResource(name);
    if (log.isTraceEnabled()) {
      if (url == null) {
        log.trace("Plugin is delegating to parent for recource: {}", name);
      } else {
        log.trace("Plugin loader is loading resource: {}", name);
      }
    }

    if (url == null) {
      // If not found delegate.
      ClassLoader parent = getParentForRequest(name, true);
      url = parent.getResource(name);
      if (log.isTraceEnabled()) {
        if (url == null) {
          log.trace("Resource not found by shared classloader: {}", name);
        } else {
          log.trace("Resource loaded by shared classloader: {}", name);
        }
      }
    }
    return url;
  }

  /**
   * Select the parent classloader to process the request. This method can decide to skip the AppClassLoader to avoid,
   * that a plugin can access classes from the classpath of the engine. Only the package prefixes configured in this
   * classloader are allowed to use access the AppClassLoader.
   *
   * @param name
   *        The requested resource name
   * @return
   */
  private ClassLoader getParentForRequest(String name, boolean resource) {
    return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
      @Override
      public ClassLoader run() {

        for (String allowed : allowedPackagePrefixes) {
          if (resource) {
            allowed = allowed.replace(".", "/");
            allowed += "/";
          } else {
            allowed += ".";
          }
          if (name.startsWith(allowed)) {
            return getParent();
          }
        }
        return ClassLoader.getSystemClassLoader()
            .getParent();
      }
    });
  }

  @SuppressWarnings({
      "unchecked"
  })
  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if (log.isTraceEnabled()) {
      log.trace("Plugin loader is loading: {}", name);
    }

    @SuppressWarnings("rawtypes")
    Enumeration[] tmp = new Enumeration[2];

    tmp[0] = findResources(name);
    if (tmp[0] == null) {
      if (log.isTraceEnabled()) {
        log.trace("Delegating to shared classloader: {}", name);
      }
      ClassLoader parent = getParentForRequest(name, true);
      tmp[0] = parent.getResources(name);
    } else {
      if (log.isTraceEnabled()) {
        log.trace("Adding also results from shared classloader: {}", name);
      }
      ClassLoader parent = getParentForRequest(name, true);
      tmp[1] = parent.getResources(name);
    }

    return new CompoundEnumeration<>(tmp);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (log.isTraceEnabled()) {
      log.trace("Plugin loader is loading: {}", name);
    }

    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c == null) {
        // If not loaded, then invoke findClass to search for that class in this repository first.
        try {
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          // ClassNotFoundException thrown if class not found by this classloader
          // Then delegate to parent
          if (log.isTraceEnabled()) {
            log.trace("Delegating to shared classloader: {}", name);
          }
          ClassLoader parent = getParentForRequest(name, false);
          c = parentLoadClass(parent, name, false);
        }
      }

      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }

}
