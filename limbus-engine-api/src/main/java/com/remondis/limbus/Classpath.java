package com.remondis.limbus;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is useful to set up an URL set representing a classpath.
 *
 * @author schuettec
 *
 */
public class Classpath {

  protected String deployName;

  /**
   * Holds the set of URLs identifying the files inside this classpath.
   */
  protected Set<URL> urlSet;

  protected Classpath() {
    this(null);
  }

  protected Classpath(String deployName) {
    this.deployName = deployName;
    this.urlSet = new HashSet<>();
  }

  /**
   * @return Returns the deploy name or <code>null</code> if the container has not assigned a symbolic id to this
   *         classpath.
   */
  public String getDeployName() {
    return deployName;
  }

  /**
   * @param deployName
   *        Used by the container to specify a symbolic id for this classpath.
   */
  protected void setDeployName(String deployName) {
    this.deployName = deployName;
  }

  /**
   * @return Returns <code>true</code> if the container has assigned a symbolic id to this classpath, otherwise
   *         <code>false</code> is returned.
   */
  public boolean hasDeployName() {
    return deployName != null;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#clone()
   */
  @Override
  public Classpath clone() {
    Classpath classpath = new Classpath();
    classpath.urlSet = new HashSet<>(this.urlSet);
    return classpath;
  }

  public static Classpath create() {
    return new Classpath();
  }

  public static Classpath create(String deployName) {
    return new Classpath(deployName);
  }

  public Classpath add(List<URL> urls) {
    urlSet.addAll(urls);
    return this;
  }

  public Classpath add(URL... url) {
    urlSet.addAll(Arrays.asList(url));
    return this;
  }

  public Classpath add(File... files) {
    for (File f : files) {
      try {
        urlSet.add(f.toURI()
            .toURL());
      } catch (MalformedURLException e) {
        throw new RuntimeException("An illegal URL was passed to this method.");
      }
    }
    return this;
  }

  public Classpath addAllFilesInDirectory(File folder) {
    if (folder.isDirectory()) {
      File[] files = folder.listFiles();
      return add(files);
    } else {
      throw new IllegalArgumentException("The specified file object must represent a directory.");
    }
  }

  public Set<URL> getClasspath() {
    return new HashSet<URL>(urlSet);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((urlSet == null) ? 0 : urlSet.hashCode());
    return result;
  }

  // CHECKSTYLE:OFF
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Classpath other = (Classpath) obj;
    if (urlSet == null) {
      if (other.urlSet != null)
        return false;
    } else if (!urlSet.equals(other.urlSet))
      return false;
    return true;
  }
  // CHECKSTYLE:ON

  @Override
  public String toString() {
    return "Classpath [urlSet=" + urlSet + "]";
  }

}
