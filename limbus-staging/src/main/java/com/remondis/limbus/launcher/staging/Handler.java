package com.remondis.limbus.launcher.staging;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.remondis.limbus.launcher.LimbusStage;
import com.remondis.limbus.utils.Lang;

public class Handler extends URLStreamHandler {

  public static Handler CURRENT_INSTANCE;

  static String CONTENT_LENGTH = "content-length";
  static String CONTENT_TYPE = "content-type";
  static String LAST_MODIFIED = "last-modified";

  private static Object lock = new Object();

  private Map<URL, byte[]> resources;

  public Handler() {
    this.resources = new ConcurrentHashMap<URL, byte[]>();
    if (CURRENT_INSTANCE == null) {
      CURRENT_INSTANCE = this;
    } else {
      throw new IllegalStateException("A stream handler was already created.");
    }
  }

  /**
   * Adds a resource to this handler specified by {@link URL}.
   *
   * @param url
   *        The resource URL to add.
   * @param resource
   *        The resource to add.
   */
  public void addResource(URL url, byte[] resource) {
    resources.put(url, resource);
  }

  /**
   * Removes a resource from the handler specified by {@link URL}.
   *
   * @param url
   *        The resource URL to remove.
   */
  public void removeResource(URL url) {
    resources.remove(url);
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    synchronized (lock) {
      try {
        byte[] fileContent;
        fileContent = resources.get(u);
        if (fileContent == null) {
          throw new IOException("No deployment for URL " + u.toString());
        }
        StreamConnection connection = new StreamConnection(u, fileContent);
        connection.connect();
        return connection;
      } catch (Exception e) {
        throw new IOException("Cannot connect to in-memory filesystem.", e);
      }
    }
  }

  /**
   * This method installs the stream handler to make streamed resources available through URLs in this JVM.
   */
  public static void installURLStreamHandler() {
    String property = System.getProperty("java.protocol.handler.pkgs");
    if (Lang.isEmpty(property)) {
      property = parentPackage();
    } else {
      property = property + "|" + parentPackage();
    }
    System.setProperty("java.protocol.handler.pkgs", property);
  }

  private static String parentPackage() {
    return LimbusStage.class.getPackage()
        .getName();
  }

  /**
   * This method deinstalls the stream handler from this JVM.
   */
  public static void deinstallURLStreamHandler() {
    String property = System.getProperty("java.protocol.handler.pkgs");
    if (property != null) {
      if (property.contains(parentPackage())) {
        property.replaceAll(parentPackage(), "");
      }
      if (property.contains("||")) {
        property.replaceAll("||", "|");
      }
    }
    System.setProperty("java.protocol.handler.pkgs", property);

  }
}
